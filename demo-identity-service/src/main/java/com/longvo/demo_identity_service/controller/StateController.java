package com.longvo.demo_identity_service.controller;

import com.longvo.demo_identity_service.dto.response.ApiResponse;
import com.longvo.demo_identity_service.dto.response.CountryResponse;
import com.longvo.demo_identity_service.dto.response.StateResponse;
import com.longvo.demo_identity_service.service.StateService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/states")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class StateController {

    StateService stateService;

    @GetMapping("/{countryCode}")
    ApiResponse<List<StateResponse>> getStates(@PathVariable("countryCode") String countryCode) {
        return ApiResponse.<List<StateResponse>>builder()
                .result(stateService.getStatesByCountryCode(countryCode))
                .build();
    }
}
