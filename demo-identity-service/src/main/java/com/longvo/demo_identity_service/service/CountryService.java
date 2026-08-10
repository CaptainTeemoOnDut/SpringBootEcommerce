package com.longvo.demo_identity_service.service;

import com.longvo.demo_identity_service.dto.response.CountryResponse;
import com.longvo.demo_identity_service.entity.Country;
import com.longvo.demo_identity_service.mapper.CountryMapper;
import com.longvo.demo_identity_service.repository.CountryRepository;
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
public class CountryService {

    @Autowired
    CountryRepository countryRepository;
    CountryMapper countryMapper;

    public List<CountryResponse> getCountries() {

        return countryRepository.findAll().stream().map(countryMapper::toCountryResponse).toList();
    }
}
