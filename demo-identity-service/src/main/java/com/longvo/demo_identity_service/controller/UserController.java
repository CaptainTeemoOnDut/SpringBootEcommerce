package com.longvo.demo_identity_service.controller;

import com.longvo.demo_identity_service.dto.request.ChangePasswordRequest;
import com.longvo.demo_identity_service.dto.request.CreatePasswordRequest;
import com.longvo.demo_identity_service.dto.request.UserCreationRequest;
import com.longvo.demo_identity_service.dto.request.UserUpdateRequest;
import com.longvo.demo_identity_service.dto.response.ApiResponse;
import com.longvo.demo_identity_service.dto.response.UserProfileResponse;
import com.longvo.demo_identity_service.dto.response.UserResponse;
import com.longvo.demo_identity_service.repository.UserRepository;
import com.longvo.demo_identity_service.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserController {
    UserService userService;
    private final UserRepository userRepository;

    @PostMapping
        ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request) {
            return ApiResponse.<UserResponse>builder()
                    .result(userService.createUser(request))
                    .build();
    }

    @PostMapping("/password")
    ApiResponse<Void> createPassword(@RequestBody @Valid CreatePasswordRequest request) {
        userService.createPassword(request);
        return ApiResponse.<Void>builder()
                .message("Password has been created")
                .build();
    }

    @PutMapping("/password")
    ApiResponse<Void> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        userService.changePassword(request);
        return ApiResponse.<Void>builder()
                .message("Password has been updated")
                .build();
    }

    @GetMapping("/profile/{userName}")
    ApiResponse<UserProfileResponse> getProfile(@PathVariable String userName) {
        return ApiResponse.<UserProfileResponse>builder()
                .result(userService.getProfile(userName))
                .build();
    }

    @GetMapping("/{userId}")
    ApiResponse<UserResponse> getUser(@PathVariable("userId") Long userId) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getUser(userId))
                .build();
    }

    @GetMapping
    ApiResponse<List<UserResponse>> getUsers() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        log.info("Username: {}", authentication.getName());
        authentication.getAuthorities().forEach(grantedAuthority -> log.info(grantedAuthority.getAuthority()));
        return ApiResponse.<List<UserResponse>>builder()
                .result(userService.getUsers())
                .build();
    }

    @GetMapping("/myInfo")
    ApiResponse<UserResponse> getMyInfo() {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getMyInfo())
                .build();
    }

    @DeleteMapping("/{userId}")
    ApiResponse<String> deleteUser(@PathVariable("userId") Long userId) {
        userService.deleteUser(userId);
        return ApiResponse.<String>builder()
                .result("User has been deleted").build();
    }

    @PutMapping("/{userId}")
    ApiResponse<UserResponse> updateUser(@PathVariable("userId") Long userId, UserUpdateRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateUser(userId, request))
                .build();
    }

    @PutMapping("suspend/{userId}")
    ApiResponse<UserResponse> suspendUser(@PathVariable("userId") Long userId, String reason) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.suspendUser(userId, reason))
                .build();
    }

    @PutMapping("activate/{userId}")
    ApiResponse<UserResponse> activateUser(@PathVariable("userId") Long userId) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.activateUser(userId))
                .build();
    }
}
