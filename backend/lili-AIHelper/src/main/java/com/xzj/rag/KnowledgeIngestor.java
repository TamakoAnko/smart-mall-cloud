package com.xzj.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 启动时把知识库目录（lili.ai.rag.knowledge-dir）里的 md/txt 灌进向量库。
 * 只有打开 lili.ai.rag.enabled=true 时才生效。
 */
@Component
@ConditionalOnProperty(prefix = "lili.ai.rag", name = "enabled", havingValue = "true")
public class KnowledgeIngestor implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeIngestor.class);
    private static final int MAX_CHUNK_LENGTH = 600;

    private final VectorStore vectorStore;

    @Value("${lili.ai.rag.knowledge-dir:./ai-knowledge}")
    private String knowledgeDir;

    public KnowledgeIngestor(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Path directory = Path.of(knowledgeDir);
        if (!Files.isDirectory(directory)) {
            log.warn("知识库目录不存在，跳过灌数据：{}", directory.toAbsolutePath());
            return;
        }

        List<Document> documents = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(directory, 2)) {
            for (Path file : paths.filter(Files::isRegularFile).toList()) {
                String name = file.getFileName().toString().toLowerCase();
                if (!name.endsWith(".md") && !name.endsWith(".txt")) {
                    continue;
                }
                for (String chunk : split(Files.readString(file))) {
                    documents.add(new Document(chunk, Map.of("source", file.getFileName().toString())));
                }
            }
        }

        if (documents.isEmpty()) {
            log.warn("知识库里没有可用的 md/txt 文件：{}", directory.toAbsolutePath());
            return;
        }
        vectorStore.add(documents);
        log.info("知识库已灌入 {} 个片段（来自 {}）", documents.size(), directory.toAbsolutePath());
    }

    /** 按空行切段，再把小段合并到 600 字以内，避免整篇文档当成一个片段 */
    private List<String> split(String text) {
        List<String> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String paragraph : text.split("\\R\\s*\\R")) {
            if (paragraph.isBlank()) {
                continue;
            }
            if (current.length() > 0 && current.length() + paragraph.length() > MAX_CHUNK_LENGTH) {
                chunks.add(current.toString());
                current.setLength(0);
            }
            if (current.length() > 0) {
                current.append("\n\n");
            }
            current.append(paragraph);
        }
        if (current.length() > 0) {
            chunks.add(current.toString());
        }
        return chunks;
    }
}
