package com.devteria.chat.repository.httpclient;

import com.devteria.chat.dto.ApiResponse;
import com.devteria.chat.dto.request.IntrospectRequest;
import com.devteria.chat.dto.response.IntrospectResponse;
import com.devteria.chat.dto.response.UserProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "demo-identity-service", url = "${app.services.identity.url}")
public interface IdentityClient {
    //@GetMapping("/internal/users/{userId}")
    @GetMapping("/auth/introspect")
    ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request);
}
