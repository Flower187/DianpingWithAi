package com.xue.aiagent_me.rag;

import com.xue.aiagent_me.rag.AppDocumentLoader;
import jakarta.annotation.Resource;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;

/**
 * PgVector 向量存储 配置类
 */
@Configuration
public class PgVectorVectorStoreConfig {

    @Resource
    private AppDocumentLoader appDocumentLoader;

    @Bean
    @ConditionalOnProperty(name = "ai.vector.enable-pgvector", havingValue = "true")
    public VectorStore pgVectorVectorStore(JdbcTemplate jdbcTemplate,
                                           EmbeddingModel dashscopeEmbeddingModel) throws IOException {

        VectorStore vectorStore = PgVectorStore.builder(jdbcTemplate, dashscopeEmbeddingModel)
                .dimensions(1536)                    // Optional: defaults to model dimensions or 1536
                .distanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE)       // Optional: defaults to COSINE_DISTANCE
                .indexType(PgVectorStore.PgIndexType.HNSW)                     // Optional: defaults to HNSW
                .initializeSchema(true)              // Optional: defaults to false
                .schemaName("public")                // Optional: defaults to "public"
                .vectorTableName("vector_store")     // Optional: defaults to "vector_store"
                .maxDocumentBatchSize(10000)         // Optional: defaults to 10000
                .build();
        // 下面这行按需调用，不然每次启动项目都会将本地md文件添加到向量数据库中
        //vectorStore.add(appDocumentLoader.loadMarkdowns());
        return vectorStore;
    }

}
