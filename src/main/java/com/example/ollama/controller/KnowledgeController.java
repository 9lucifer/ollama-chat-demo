package com.example.ollama.controller;

import com.example.ollama.infrastruct.api.KnowledgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    @PostMapping("/ingest")
    public String ingest(@RequestParam String text) {
        knowledgeService.ingest(text);
        return "成功写入 Milvus";
    }

    @GetMapping("/query")
    public String query(@RequestParam String text){
        knowledgeService.query(text);
        return "ok";
    }
}
