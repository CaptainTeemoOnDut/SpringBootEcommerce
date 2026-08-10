package com.longvo.demo_identity_service.repository.httpclient;

import com.longvo.demo_identity_service.dto.request.ExchangeTokenRequest;
import com.longvo.demo_identity_service.dto.response.ExchangeTokenResponse;
import com.longvo.demo_identity_service.dto.response.OutboundUserResponse;
import feign.QueryMap;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-client", url = "https://openidconnect.googleapis.com")
public interface OutboundUserClient {

    @GetMapping("/v1/userinfo")
    OutboundUserResponse getUserInfo(@RequestHeader("Authorization") String bearerToken);
}
/*@FeignClient(name = "outbound-user-identity", url = "https://oauth2.googleapis.com")
public interface OutboundUserClient {
    @GetMapping(value = "/oauth2/v1/userinfo")
    Object getUserInfo(@RequestParam("alt") String alt,
                       @RequestParam("access_token") String accessToken);
}*/
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

