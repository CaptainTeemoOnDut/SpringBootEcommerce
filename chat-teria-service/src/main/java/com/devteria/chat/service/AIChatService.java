package com.devteria.chat.service;

import com.corundumstudio.socketio.SocketIOServer;
import com.devteria.chat.Tool.ProductTools;
import com.devteria.chat.dto.request.AIChatRequest;
import com.devteria.chat.dto.request.ChatMessageRequest;
import com.devteria.chat.dto.response.ChatMessageResponse;
import com.devteria.chat.entity.ChatMessage;
import com.devteria.chat.entity.ParticipantInfo;
import com.devteria.chat.entity.WebSocketSession;
import com.devteria.chat.enums.ChatMessageStatus;
import com.devteria.chat.mapper.ChatMessageMapper;
import com.devteria.chat.repository.WebSocketSessionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AIChatService {
    private final ChatClient chatClient;
    private final RagService ragService;
    public WebSocketSessionRepository webSocketSessionRepository;
    ChatMessageMapper chatMessageMapper;
    SocketIOServer socketIOServer;
    ObjectMapper objectMapper;

    public AIChatService(ChatClient.Builder builder,
                         ProductTools productTools,
                         RagService ragService,
                         WebSocketSessionRepository webSocketSessionRepository,
                         ChatMessageMapper chatMessageMapper,
                         SocketIOServer socketIOServer,
                         ObjectMapper objectMapper) {

        this.chatClient = builder
                .defaultTools(productTools)
                .build();

        this.ragService = ragService;
        this.webSocketSessionRepository = webSocketSessionRepository;
        this.chatMessageMapper = chatMessageMapper;
        this.socketIOServer = socketIOServer;
        this.objectMapper = objectMapper;
    }

    public ChatMessageResponse chat(ChatMessageRequest request) {

        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        String userQuery = request.getMessage();

        // 1. Lấy RAG context
        String context = ragService.search(userQuery);

        // 2. Xây dựng System Prompt linh hoạt theo Context
        StringBuilder systemPromptBuilder = new StringBuilder("""
            Bạn là trợ lý AI chăm sóc khách hàng chính thức của sàn Thương mại Điện tử [Tên_Sàn_Của_Bạn].
            Mục tiêu của bạn là hỗ trợ người dùng nhiệt tình, lịch sự, ngắn gọn và chuyên nghiệp.

            QUY TẮC PHẢN HỒI:
            1. Nếu người dùng hỏi về sản phẩm (tìm kiếm, giá cả, tồn kho, thông số), hãy sử dụng các Tools/Functions được cung cấp.
            2. Nếu người dùng chào hỏi hoặc trò chuyện xã giao, hãy đáp lại thân thiện.
            3. Nếu người dùng hỏi về chính sách/hướng dẫn, HÃY ƯU TIÊN SỬ DỤNG THÔNG TIN TRONG PHẦN 'CONTEXT' DƯỚI ĐÂY.
            4. TUYỆT ĐỐI KHÔNG tự bịa đặt chính sách, hotline, hoặc mã giảm giá không có trong dữ liệu.
            5. Định dạng câu trả lời rõ ràng bằng Markdown (dùng gạch đầu dòng, bôi đậm từ khóa chính).
            """);

        if (context != null && !context.isBlank()) {
            systemPromptBuilder.append("""

                --- CONTEXT THAM KHẢO ---
                %s
                ------------------------
                Nói rõ ràng dựa trên CONTEXT trên. Nếu thông tin không thể trả lời từ CONTEXT hoặc Tools, hãy hướng dẫn khách hàng liên hệ Hotline: 1900-xxxx hoặc Email: cskh@domain.com.
                """.formatted(context));
        } else {
            systemPromptBuilder.append("""
                
                LƯU Ý: Hiện không có tài liệu nội bộ nào cho câu hỏi này. Nếu người dùng hỏi về quy định/chính sách cụ thể mà bạn không chắc chắn, hãy lịch sự từ chối và hướng dẫn họ liên hệ bộ phận CSKH trực tiếp.
                """);
        }

        SystemMessage systemMessage = new SystemMessage(systemPromptBuilder.toString());
        UserMessage userMessage = new UserMessage(userQuery);

        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

        // 3. Gọi AI Client
        String response = chatClient
                .prompt(prompt)
                .call()
                .content();

        // 4. Lưu tin nhắn của AI Bot
        ChatMessage aiChatMessage = chatMessageMapper.toChatMessage(request);
        aiChatMessage.setId(UUID.randomUUID().toString());
        aiChatMessage.setMessage(response);

        // SỬA LỖI: Sender ở đây phải là BOT chứ không phải userName của khách
        aiChatMessage.setSender(ParticipantInfo.builder()
                .userId("system-bot-id")
                .userName("AI Assistant")
                .firstName("Trợ lý")
                .lastName("AI")
                .avatar("https://cdn-icons-png.flaticon.com/512/6008/6008363.png")
                .build());
        aiChatMessage.setCreatedDate(Instant.now());

        log.info("Created AI chat message {}", aiChatMessage);

        // 5. Bắn Socket CHỈ CHO USER ĐÓ (Tránh broadcast toàn hệ thống)
        ChatMessageResponse chatMessageResponse = chatMessageMapper.toChatMessageResponse(aiChatMessage);
        chatMessageResponse.setStatus(ChatMessageStatus.SENT);
        chatMessageResponse.setMe(false); // Đây là tin nhắn người khác (Bot) gửi tới User

        var webSocketSession = webSocketSessionRepository.findByUserName(userName);
        if (Objects.nonNull(webSocketSession)) {
            try {
                String messageJson = objectMapper.writeValueAsString(chatMessageResponse);

                // Tìm chính xác client session của User thay vì getAllClients()
                socketIOServer.getClient(UUID.fromString(webSocketSession.getSocketSessionId()))
                        .sendEvent("message", messageJson);

            } catch (JsonProcessingException e) {
                log.error("Error serializing chat response", e);
            }
        }

        return chatMessageResponse;
    }

    /*public ChatMessageResponse chat(ChatMessageRequest request) {

        String userName = SecurityContextHolder.getContext().getAuthentication().getName();

        UserMessage userMessage = new UserMessage(request.getMessage());

        // 🔥 RAG context
        String context = ragService.search(request.getMessage());

        SystemMessage systemMessage = new SystemMessage("""
                Bạn là trợ lý mua sắm cho một nền tảng thương mại điện tử.
                Nếu người dùng hỏi về dữ liệu sản phẩm (giá cả, tồn kho, danh sách sản phẩm),
                hãy sử dụng các công cụ. Nếu người dùng hỏi về chính sách, hãy sử dụng ngữ cảnh bên dưới.
                Nếu thông tin không có trong CONTEXT, hãy lịch sự từ chối và hướng dẫn người dùng liên hệ CSKH qua hotline.
                         CONTEXT:
                                %s
                """.formatted(context));



        Prompt prompt = new Prompt(systemMessage, userMessage);

        String response =  chatClient
                .prompt(prompt)
                .call()
                .content();

        // Publish socket event to clients

        // Build Chat message Info
        ChatMessage chatMessage = chatMessageMapper.toChatMessage(request);
        chatMessage.setMessage(response);
        chatMessage.setSender(ParticipantInfo.builder()
                .userId("")
                .userName(userName)
                .firstName("")
                .lastName("")
                .avatar("https://cdn-icons-png.flaticon.com/512/6008/6008363.png")
                .build());
        chatMessage.setCreatedDate(Instant.now());
        chatMessage.setId(UUID.randomUUID().toString());
        log.info("Created chat message {}", chatMessage);

        var webSocketSession =
                webSocketSessionRepository.findByUserName(userName);
        ChatMessageResponse chatMessageResponse = chatMessageMapper.toChatMessageResponse(chatMessage);
        chatMessageResponse.setStatus(ChatMessageStatus.SENT);
        socketIOServer.getAllClients().forEach(client -> {

            if (Objects.nonNull(webSocketSession)) {
                String message = null;
                try {
                    chatMessageResponse.setMe(false);
                    message = objectMapper.writeValueAsString(chatMessageResponse);
                    client.sendEvent("message", message);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }

        });

        // convert to Response
        return toChatMessageResponse(chatMessage);
    }*/

    private ChatMessageResponse toChatMessageResponse(ChatMessage chatMessage) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        var chatMessageResponse = chatMessageMapper.toChatMessageResponse(chatMessage);

        chatMessageResponse.setMe(userName.equals(chatMessage.getSender().getUserName()));

        return chatMessageResponse;
    }
}
