package com.xzj.rag;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RAG（向量检索）配置，默认关闭（lili.ai.rag.enabled=false）。
 *
 * 打开前置条件：容器里必须有一个 EmbeddingModel bean，例如：
 *   1) pom 加 spring-ai-starter-model-ollama
 *   2) 本地 Ollama 执行 ollama pull nomic-embed-text
 *   3) application.yaml 里 lili.ai.rag.enabled=true
 * 详细步骤见 patch 目录的 README.md。
 *
 * 这里用的是 Spring AI 自带的 SimpleVectorStore（内存向量库，可 save/load 成文件），
 * 不需要额外下载 Redis Stack / Milvus 这类向量数据库。
 */
@Configuration
@ConditionalOnProperty(prefix = "lili.ai.rag", name = "enabled", havingValue = "true")
public class AiRagConfig {

    @Bean
    public SimpleVectorStore aiKnowledgeVectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    @Bean
    public RetrievalAugmentationAdvisor aiKnowledgeAdvisor(SimpleVectorStore aiKnowledgeVectorStore) {
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .vectorStore(aiKnowledgeVectorStore)
                        .similarityThreshold(0.45)
                        .topK(4)
                        .build())
                .build();
    }
}
