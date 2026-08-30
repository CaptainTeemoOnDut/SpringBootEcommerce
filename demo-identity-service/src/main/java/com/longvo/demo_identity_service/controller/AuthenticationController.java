package com.longvo.demo_identity_service.controller;

import com.longvo.demo_identity_service.dto.request.AuthenticationRequest;
import com.longvo.demo_identity_service.dto.request.IntrospectRequest;
import com.longvo.demo_identity_service.dto.request.LogoutRequest;
import com.longvo.demo_identity_service.dto.request.RefreshRequest;
import com.longvo.demo_identity_service.dto.response.ApiResponse;
import com.longvo.demo_identity_service.dto.response.AuthenticationResponse;
import com.longvo.demo_identity_service.dto.response.IntrospectResponse;
import com.longvo.demo_identity_service.service.AuthenticationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.expression.ParseException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;

    // Login with Google: front end sends Google code to this api
    @PostMapping("/outbound/authentication")
    ApiResponse<AuthenticationResponse> outboundAuthenticate(
            @RequestParam("code") String code
    ){
        var result = authenticationService.outboundAuthenticate(code);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }


    @PostMapping("/login") // create access and refresh token after login successfully
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        var result = authenticationService.authenticate(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/introspect") // create access and refresh token after login successfully
    ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request) {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> refreshToken(@RequestBody RefreshRequest request){

        var result = authenticationService.refreshToken(request.getToken());

        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody LogoutRequest request)
            throws ParseException {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder()
                .build();
    }
}

