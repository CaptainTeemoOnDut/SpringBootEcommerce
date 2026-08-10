package com.longvo.demo_identity_service.repository.httpclient;

import com.longvo.demo_identity_service.configuration.AuthenticationRequestInterceptor;
import com.longvo.demo_identity_service.dto.request.ConversationRequest;
import org.springframework.web.bind.annotation.RequestBody;
import com.longvo.demo_identity_service.dto.response.ApiResponse;
import com.longvo.demo_identity_service.dto.response.ConversationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "AppChatDemo", url = "${app.services.chat}",
        configuration = { AuthenticationRequestInterceptor.class })
public interface ChatClient {
    @PostMapping(value = "/conversations/create", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<ConversationResponse> createConversation(@RequestBody ConversationRequest request);
}
