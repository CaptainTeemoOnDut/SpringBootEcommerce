package com.example.AppChatDemo.repository.httpclient;


import com.example.AppChatDemo.dto.ApiResponse;
import com.example.AppChatDemo.dto.response.UserProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "demo-identity-service", url = "${app.services.profile.url}")
public interface ProfileClient {
    //@GetMapping("/internal/users/{userId}")
    @GetMapping("/users/{userId}")
    ApiResponse<UserProfileResponse> getProfile(@PathVariable String userId);
}
