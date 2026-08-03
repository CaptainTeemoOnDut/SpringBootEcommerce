package com.devteria.chat.dto.response;

import com.devteria.chat.entity.ParticipantInfo;
import com.devteria.chat.enums.ChatMessageStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatMessageResponse {
    String id;
    String conversationId;
    boolean me;
    String message;
    ParticipantInfo sender;
    Instant createdDate;
    ChatMessageStatus status;
}
