package com.devteria.chat.configuration;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class PgVectorConfig {

    // Spring Boot sẽ tự động lấy JdbcTemplate và EmbeddingModel
    // dựa trên cấu hình trong file application.yaml của bạn.
    @Bean
    public VectorStore vectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .dimensions(768) // Đảm bảo khớp với model embedding
                .vectorTableName("vector_store")
                .initializeSchema(true) // Quan trọng: Tự động tạo bảng nếu chưa có
                .build();
    }
}
