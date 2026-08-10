package com.longvo.demo_identity_service.controller;

import com.longvo.demo_identity_service.dto.request.AuthenticationRequest;
import com.longvo.demo_identity_service.dto.request.IntrospectRequest;
import com.longvo.demo_identity_service.dto.request.LogoutRequest;
import com.longvo.demo_identity_service.dto.request.RefreshRequest;
import com.longvo.demo_identity_service.dto.response.ApiResponse;
import com.longvo.demo_identity_service.dto.response.AuthenticationResponse;
import com.longvo.demo_identity_service.dto.response.ExchangeTokenResponse;
import com.longvo.demo_identity_service.dto.response.IntrospectResponse;
import com.longvo.demo_identity_service.service.AuthenticationService;
import com.longvo.demo_identity_service.service.GoogleAuthService;
import com.longvo.demo_identity_service.service.RedisRefreshTokenService;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.expression.ParseException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;
    RedisRefreshTokenService redisRefreshTokenService;
    GoogleAuthService googleAuthService;

    @NonFinal
    @Value("${google.client-id}")
    protected String CLIENT_ID;

    @NonFinal
    @Value("${google.redirect-uri}")
    protected String REDIRECT_URI;

    @NonFinal
    @Value("${app.frontend-url}")
    private String frontendUrl;

    @NonFinal
    @Value("${app.frontend-success-path}")
    private String frontendSuccessPath;

    // Step 1: redirect user to Google Authorization endpoint (backend-initiated)
    @GetMapping("/google/login")
    public void redirectToGoogle(HttpServletResponse response) throws IOException {

        String authUrl = "https://accounts.google.com/o/oauth2/v2/auth" +
                "?client_id=" + CLIENT_ID +
                "&redirect_uri=" + REDIRECT_URI +
                "&response_type=code" +
                "&scope=openid%20email%20profile" +
                "&access_type=offline" + // request refresh token
                "&prompt=consent";

        System.out.println(authUrl);
        response.sendRedirect(authUrl);
    }

    // Step 2: Google redirects here with code
    /*@GetMapping("/google/callback")
    public void callback(@RequestParam("code") String code, HttpServletResponse response) throws IOException {
        try {
            System.out.println("HA HA THIS IS OUR CODE: " + code);
            String jwt = googleAuthService.handleCodeAndReturnJwt(code);
            System.out.println("HA HA THIS IS OUR JWT: " + jwt);
            // Redirect to frontend with JWT as query param OR set secure cookie.
            String target = frontendUrl + frontendSuccessPath + "?token=" + URLEncoder.encode(jwt, StandardCharsets.UTF_8);
            response.sendRedirect(target);
        } catch (Exception e) {
            // handle error, redirect to error page
            String target = frontendUrl + "/login?error=oauth_failed";
            response.sendRedirect(target);
        }
    }*/

    @PostMapping("/outbound/authentication")
    ApiResponse<AuthenticationResponse> outboundAuthenticate(
            @RequestParam("code") String code
    ){
        var result = authenticationService.outboundAuthenticate(code);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @PostMapping("/delete")
    String deleteToken(@RequestBody String token) {
        redisRefreshTokenService.delete(token);
        return "token is deleted";
    }

    @PostMapping("/token") // create access and refresh token after login successfully
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        var result = authenticationService.authenticate(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/introspect") // verify access token
    ApiResponse<IntrospectResponse> authenticate(@RequestBody IntrospectRequest request) throws java.text.ParseException, JOSEException {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder().result(result).build();
    }

    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> refreshToken(@RequestBody RefreshRequest request) throws java.text.ParseException, JOSEException {
        //String refreshToken = (String) request.getAttribute("refreshToken");

        System.out.println("🔁 Received refresh token = " + request.getToken());

        var result = authenticationService.refreshToken(request.getToken());
        // Tạo cookie cho refresh token mới
        /*ResponseCookie cookie = ResponseCookie.from("refreshToken", result.getRefreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/identity/auth/refresh")
                .sameSite("None")
                .maxAge(Duration.ofDays(7))
                .build();

        // Gửi cookie xuống client
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());*/

        // Chỉ trả về access token trong body (refresh token đã nằm trong cookie)
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody LogoutRequest request)
            throws ParseException, JOSEException, java.text.ParseException {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder()
                .build();
    }
}

