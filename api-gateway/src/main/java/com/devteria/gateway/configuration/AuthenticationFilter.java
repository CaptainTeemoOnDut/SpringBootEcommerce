package com.devteria.gateway.configuration;

import com.devteria.gateway.dto.ApiResponse;
import com.devteria.gateway.service.IdentityService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PACKAGE, makeFinal = true)
public class AuthenticationFilter implements GlobalFilter, Ordered {
    IdentityService identityService;
    ObjectMapper objectMapper;

    // Dùng AntPathMatcher thay vì RegEx thuần để xử lý URL chuẩn Spring
    AntPathMatcher pathMatcher = new AntPathMatcher();

    @NonFinal
    private String[] publicEndpoints = {
            "/identity/auth/**",
            "/identity/users/**",
            "/notification/email/send",
            "/file/media/download/**",
            "/identity/products/**",
            "/identity/product-categories/**",
            "/identity/reviews/**",
            "/identity/media/**"
    };

    @Value("${app.api-prefix:}") // Đặt giá trị mặc định là rỗng nếu không tìm thấy key
    @NonFinal
    private String apiPrefix;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("=== Enter AuthenticationFilter ===");

        if (isPublicEndpoint(exchange.getRequest())) {
            log.info("-> Request is Public, skipping authentication filter.");
            return chain.filter(exchange);
        }

        log.info("-> Request is Private, checking authorization header...");

        // Get token from authorization header
        List<String> authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION);
        if (CollectionUtils.isEmpty(authHeader)) {
            log.warn("Missing Authorization Header");
            return unauthenticated(exchange.getResponse());
        }

        String token = authHeader.getFirst().replace("Bearer ", "");

        return identityService.introspect(token)
                .flatMap(introspectResponse -> {
                    if (introspectResponse.getResult().isValid()) {
                        return chain.filter(exchange);
                    } else {
                        log.info("Authentication unsuccessful");
                        return unauthenticated(exchange.getResponse());
                    }
                })
                .onErrorResume(throwable -> {
                    log.error("Error when introspect token", throwable);
                    return unauthenticated(exchange.getResponse());
                });
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private boolean isPublicEndpoint(ServerHttpRequest request) {
        try {
            String originalPath = request.getURI().getPath();
            String path = originalPath;

            log.info("[DEBUG] Configured apiPrefix: '{}'", apiPrefix);
            log.info("[DEBUG] Original Request Path: '{}'", originalPath);

            // 1. Loại bỏ apiPrefix khỏi URL
            if (StringUtils.hasText(apiPrefix) && path.startsWith(apiPrefix)) {
                path = path.substring(apiPrefix.length());
            }

            log.info("[DEBUG] Path after stripping prefix: '{}'", path);

            // 2. So sánh bằng AntPathMatcher
            String finalPath = path;
            boolean isPublic = Arrays.stream(publicEndpoints)
                    .anyMatch(endpoint -> pathMatcher.match(endpoint, finalPath));

            log.info("[DEBUG] Result isPublicEndpoint for '{}': {}", finalPath, isPublic);
            return isPublic;

        } catch (Exception e) {
            log.error("[DEBUG ERROR] Exception occurred inside isPublicEndpoint", e);
            return false; // Nếu gặp lỗi bất ngờ, coi như private để bảo mật
        }
    }

    Mono<Void> unauthenticated(ServerHttpResponse response) {
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(1401)
                .message("Unauthenticated")
                .build();

        String body = "";
        try {
            body = objectMapper.writeValueAsString(apiResponse);
        } catch (JsonProcessingException e) {
            log.error("Error writing unauthenticated response json", e);
        }

        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }
}
