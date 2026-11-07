package com.xue.aiagent_me.rag;


import jakarta.annotation.Resource;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * 向量存储配置类
 * 构造一个 VectorStore 对象，用于存储和检索向量数据
 */
@Configuration
public class AppVectorStoreConfig {

    @Resource
    private AppDocumentLoader appDocumentLoader;

    @Bean
    VectorStore appVectorStore(EmbeddingModel dashscopeEmbeddingModel) throws IOException {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel).build();
        simpleVectorStore.add(appDocumentLoader.loadMarkdowns());
        return simpleVectorStore;
    }

}
