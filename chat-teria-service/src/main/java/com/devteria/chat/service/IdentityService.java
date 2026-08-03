package com.devteria.chat.service;

import com.corundumstudio.socketio.SocketIOServer;
import com.devteria.chat.dto.request.ChatMessageRequest;
import com.devteria.chat.dto.request.IntrospectRequest;
import com.devteria.chat.dto.response.ChatMessageResponse;
import com.devteria.chat.dto.response.IntrospectResponse;
import com.devteria.chat.entity.ChatMessage;
import com.devteria.chat.entity.ParticipantInfo;
import com.devteria.chat.exception.AppException;
import com.devteria.chat.exception.ErrorCode;
import com.devteria.chat.mapper.ChatMessageMapper;
import com.devteria.chat.repository.ChatMessageRepository;
import com.devteria.chat.repository.ConversationRepository;
import com.devteria.chat.repository.httpclient.IdentityClient;
import com.devteria.chat.repository.httpclient.ProfileClient;
import feign.FeignException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IdentityService {
   IdentityClient identityClient;

   public IntrospectResponse introspect (IntrospectRequest request) {
       try {
            var result = identityClient.introspect(request).getResult();
            if (Objects.isNull(result)) {
                return IntrospectResponse.builder()
                        .valid(false)
                        .build();
            }
            return result;
       } catch (FeignException e) {
           log.error("Introspection failed: {}", e.getMessage(), e);
           return IntrospectResponse.builder()
                   .valid(false)
                   .build();
       }

   }
}
