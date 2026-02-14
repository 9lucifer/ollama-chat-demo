package com.example.ollama.service;

import com.example.ollama.config.OllamaConfig;
import com.example.ollama.infrastruct.api.KnowledgeService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final OllamaConfig ollamaConfig;
    private final KnowledgeService knowledgeService;

    public SseEmitter chatStream(String userMessage, String model) {
        log.info("收到用户消息: {}", userMessage);
        // 1. 检索相关文档
        List<TextSegment> relevantDocs = knowledgeService.retrieve(userMessage, 5);
        StringBuilder context = new StringBuilder();
        for (TextSegment doc : relevantDocs) {
            context.append(doc.text()).append("\n");
        }
        // 2. 构造带上下文的问题
        String ragPrompt = "根据以下内容回答问题：\n" + context + "\n问题：" + userMessage;

        // 3. SSE 流式返回答案
        SseEmitter emitter = new SseEmitter(300000L); // 5分钟超时
        StringBuilder fullResponse = new StringBuilder();
        StringBuilder thinkingContent = new StringBuilder();
        StringBuilder answerContent = new StringBuilder();
        AtomicBoolean inThinking = new AtomicBoolean(false);
        AtomicBoolean thinkingSent = new AtomicBoolean(false);

        // 根据模型参数创建对应的模型实例
        StreamingChatLanguageModel modelInstance = ollamaConfig.createStreamingChatModel(model);
        modelInstance.generate(ragPrompt, new StreamingResponseHandler<AiMessage>() {
            @Override
            public void onNext(String token) {
                try {
                    fullResponse.append(token);
                    String currentText = fullResponse.toString();

                    // 检测思考标签
                    if (currentText.contains("<think>") && !inThinking.get()) {
                        inThinking.set(true);
                    }

                    if (inThinking.get()) {
                        // 累积思考内容
                        if (!token.contains("<think>") && !token.contains("</think>")) {
                            thinkingContent.append(token);
                        }

                        if (currentText.contains("</think>")) {
                            inThinking.set(false);
                            // 一次性发送完整的思考内容
                            if (!thinkingSent.get() && thinkingContent.length() > 0) {
                                emitter.send(SseEmitter.event()
                                        .name("thinking")
                                        .data(thinkingContent.toString()));
                                thinkingSent.set(true);
                            }
                        }
                    } else {
                        // 答案内容，逐字发送
                        if (!token.contains("</think>") && !token.contains("<parameter=")) {
                            answerContent.append(token);
                            // 立即发送到前端
                            emitter.send(SseEmitter.event()
                                    .name("answer")
                                    .data(token));
                        }
                    }

                } catch (IOException e) {
                    log.error("发送SSE消息失败", e);
                    emitter.completeWithError(e);
                }
            }

            @Override
            public void onComplete(Response<AiMessage> response) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("done")
                            .data(""));
                    emitter.complete();
                    log.info("流式响应完成");
                } catch (IOException e) {
                    log.error("完成SSE连接失败", e);
                    emitter.completeWithError(e);
                }
            }

            @Override
            public void onError(Throwable error) {
                log.error("流式响应错误", error);
                emitter.completeWithError(error);
            }
        });

        return emitter;
    }
}
