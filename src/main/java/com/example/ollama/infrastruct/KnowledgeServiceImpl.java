package com.example.ollama.infrastruct;

import com.example.ollama.infrastruct.api.KnowledgeService;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KnowledgeServiceImpl implements KnowledgeService {

    private final EmbeddingStore<TextSegment> store;
    private final EmbeddingModel embeddingModel;

    @Override
    public void ingest(String text) {
        Document document = Document.from(text);

        List<TextSegment> segments =
                DocumentSplitters.recursive(300, 50).split(document);

        List<dev.langchain4j.data.embedding.Embedding> embeddings =
                embeddingModel.embedAll(segments).content();

        store.addAll(embeddings, segments);

        System.out.println("写入向量库成功: " + segments.size());
    }

    @Override
    public void query(String text) {
        Response<Embedding> embeddingResponse = embeddingModel.embed(TextSegment.from(text));
        Embedding queryVector = embeddingResponse.content();
        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryVector)
                .maxResults(5)
                .minScore(0.0)
                .build();
        EmbeddingSearchResult<TextSegment> searchResult = store.search(request);

        for (EmbeddingMatch<TextSegment> match : searchResult.matches()) {
            TextSegment segment = match.embedded();      // 获取存储的文本段
            double score = match.score();                // 相似度分数
            System.out.println("Score: " + score + ", Text: " + segment.text());
        }
    }

    public List<TextSegment> retrieve(String query, int topK) {
        Response<Embedding> embeddingResponse = embeddingModel.embed(TextSegment.from(query));
        Embedding queryVector = embeddingResponse.content();
        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryVector)
                .maxResults(topK)
                .minScore(0.0)
                .build();
        EmbeddingSearchResult<TextSegment> searchResult = store.search(request);
        return searchResult.matches().stream()
                .map(EmbeddingMatch::embedded)
                .toList();
    }
}
