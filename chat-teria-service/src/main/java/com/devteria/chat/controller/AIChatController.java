package com.devteria.chat.controller;


import com.devteria.chat.dto.ApiResponse;
import com.devteria.chat.dto.request.AIChatRequest;
import com.devteria.chat.dto.request.ChatMessageRequest;
import com.devteria.chat.dto.response.ChatMessageResponse;
import com.devteria.chat.service.AIChatService;
import com.devteria.chat.service.RagService;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ai")
@CrossOrigin
public class AIChatController {
    private final AIChatService chatService;
    private final RagService ragService;

    public AIChatController(AIChatService chatService, RagService ragService) {

        this.chatService = chatService;
        this.ragService = ragService;
    }

    // init data RAG (call 1 lần)
    @GetMapping("/init")
    public String init() {
        ragService.saveDocuments();
        return "RAG initialized!";
    }


    @PostMapping("/chat")
    ApiResponse<ChatMessageResponse> chat(@RequestBody ChatMessageRequest request) {

        return ApiResponse.<ChatMessageResponse>builder()
                .result(chatService.chat(request))
                .build();
    }
}
