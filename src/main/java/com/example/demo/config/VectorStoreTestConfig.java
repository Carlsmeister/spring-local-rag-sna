package com.example.demo.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
@Profile("test")
public class VectorStoreTestConfig {

    @Bean
    @Primary
    public EmbeddingModel embeddingModel() {
        return new DummyEmbeddingModel();
    }

    @Bean
    @Primary
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    public static class DummyEmbeddingModel implements EmbeddingModel {
        @Override
        public EmbeddingResponse call(EmbeddingRequest request) {
            return new EmbeddingResponse(List.of());
        }

        @Override
        public float[] embed(String text) {
            return new float[]{1.0f};
        }

        @Override
        public float[] embed(org.springframework.ai.document.Document document) {
            return new float[]{1.0f};
        }
    }
}
