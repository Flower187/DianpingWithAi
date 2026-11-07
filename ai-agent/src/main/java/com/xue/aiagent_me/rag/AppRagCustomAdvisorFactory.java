package com.xue.aiagent_me.rag;

import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

/**
 * 自定义向量数据库检索增强 工厂类
 */
public class AppRagCustomAdvisorFactory {
    public static Advisor createAppRagCustomAdvisor(VectorStore vectorStore, String status) {
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(
                        VectorStoreDocumentRetriever.builder()
                                .vectorStore(vectorStore)
                                // 过滤表达式
                                .filterExpression(
                                        new FilterExpressionBuilder().eq("status", status).build()
                                )
                                // 相似度阈值
                                .similarityThreshold(0.5)
                                // 返回文档数量
                                .topK(3)
                                .build()
                )
                .queryAugmenter(AppContextualQueryAugmenterFactory.createInstance())
                .build();


    }

}
