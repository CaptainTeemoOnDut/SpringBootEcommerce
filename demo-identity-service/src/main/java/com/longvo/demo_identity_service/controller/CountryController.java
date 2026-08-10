package com.longvo.demo_identity_service.controller;

import com.longvo.demo_identity_service.dto.response.ApiResponse;
import com.longvo.demo_identity_service.dto.response.CountryResponse;
import com.longvo.demo_identity_service.dto.response.UserResponse;
import com.longvo.demo_identity_service.service.CountryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/countries")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CountryController {

    CountryService countryService;

    @GetMapping
    ApiResponse<List<CountryResponse>> getCountries() {
        return ApiResponse.<List<CountryResponse>>builder()
                .result(countryService.getCountries())
                .build();
    }
}
