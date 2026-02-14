package com.example.ollama.infrastruct.api;

import dev.langchain4j.data.segment.TextSegment;

import java.util.List;

public interface KnowledgeService {

    /**
     * 将文本写入向量库
     * @param text 待处理的文本
     */
    void ingest(String text);
    void query(String text);
    List<TextSegment> retrieve(String query, int topK);
}
