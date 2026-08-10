package com.longvo.demo_identity_service.repository.httpclient;

import com.longvo.demo_identity_service.dto.request.ExchangeTokenRequest;
import com.longvo.demo_identity_service.dto.response.ExchangeTokenResponse;
import feign.QueryMap;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;


@FeignClient(name = "outbound-identity", url = "https://oauth2.googleapis.com")
public interface OutboundIdentityClient {
    @PostMapping(value = "/token", produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    ExchangeTokenResponse exchangeToken(@QueryMap ExchangeTokenRequest request);
}
/*@FeignClient(
        name = "outbound-identity",
        url = "https://oauth2.googleapis.com",
        configuration = FeignFormConfig.class
)
public interface OutboundIdentityClient {

    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    ExchangeTokenResponse exchangeToken(@RequestBody MultiValueMap<String, String> form);
}*/
/*
@FeignClient(
        name = "outbound-identity",
        url = "https://oauth2.googleapis.com",
        configuration = FeignFormConfig.class
)
public interface OutboundIdentityClient {

    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    ExchangeTokenResponse exchangeToken(@RequestParam Map<String, ?> form);


}*/

