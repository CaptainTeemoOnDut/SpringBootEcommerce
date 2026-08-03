package com.example.AppChatDemo.mapper;

import com.example.AppChatDemo.dto.response.ConversationResponse;
import com.example.AppChatDemo.entity.Conversation;
import org.mapstruct.Mapper;


import java.util.List;

@Mapper(componentModel = "spring")
public interface ConversationMapper {
    ConversationResponse toConversationResponse(Conversation conversation);

    List<ConversationResponse> toConversationResponseList(List<Conversation> conversations);
}
