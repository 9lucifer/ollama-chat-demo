package com.example.ollama.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class OllamaConfig {

    @Value("${ollama.base-url}")
    private String ollamaUrl;

    @Value("${ollama.model-name}")
    private String modelName;

    @Value("${ollama.timeout}")
    private int timeout;

    @Value("${milvus.host}")
    private String host;

    @Value("${milvus.port}")
    private Integer port;

    @Value("${ollama.embedding-model}")
    private String embeddingModel;

    // 支持的模型列表
    private static final Map<String, String> SUPPORTED_MODELS = new HashMap<>();
    static {
        SUPPORTED_MODELS.put("deepseek", "deepseek-r1:7b");
        SUPPORTED_MODELS.put("qwen", "qwen2:7b");
    }

    // 模型工厂，根据模型名称获取对应的模型实例
    public StreamingChatLanguageModel createStreamingChatModel(String modelKey) {
        String actualModelName = SUPPORTED_MODELS.getOrDefault(modelKey, modelName);
        return OllamaStreamingChatModel.builder()
                .baseUrl(ollamaUrl)
                .modelName(actualModelName)
                .timeout(Duration.ofSeconds(timeout))
                .build();
    }

    @Bean
    public StreamingChatLanguageModel streamingChatLanguageModel() {
        return createStreamingChatModel("deepseek");
    }

    // 连接milvus
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore(){
        return MilvusEmbeddingStore.builder()
                .host(host)
                .port(port)
                .collectionName("knowledge")
                .dimension(768)
                .build();
    }

    // embedding 模型
    @Bean
    public EmbeddingModel embeddingModel(){
        return OllamaEmbeddingModel.builder()
                .baseUrl(ollamaUrl)
                .modelName(embeddingModel)
                .build();
    }
}
