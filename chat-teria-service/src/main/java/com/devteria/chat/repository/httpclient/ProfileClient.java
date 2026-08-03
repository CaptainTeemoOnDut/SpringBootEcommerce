package com.devteria.chat.repository.httpclient;

import com.devteria.chat.dto.ApiResponse;
import com.devteria.chat.dto.response.UserProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "demo-profile-service", url = "${app.services.profile.url}")
public interface ProfileClient {
    //@GetMapping("/internal/users/{userId}")
    @GetMapping("/users/profile/{userName}")
    ApiResponse<UserProfileResponse> getProfile(@PathVariable String userName);
}
