package com.longvo.demo_identity_service.service;

import com.longvo.demo_identity_service.dto.response.StateResponse;
import com.longvo.demo_identity_service.mapper.StateMapper;
import com.longvo.demo_identity_service.repository.StateRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class StateService {

    @Autowired
    StateRepository stateRepository;
    StateMapper stateMapper;

    public List<StateResponse> getStatesByCountryCode(String countryCode) {

        return stateRepository.findByCountryCode(countryCode).stream().map(stateMapper::toStateResponse).toList();
    }
}
