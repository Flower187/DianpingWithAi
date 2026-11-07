package com.xue.aiagent_me.rag;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Rag云知识库Advisor配置类
 * 构造一个 RetrievalAugmentationAdvisor对象，用于增强检索能力
 */
@Configuration
public class AppRagCloudAdvisorConfig {

    @Value("${spring.ai.dashscope.api-key}")
    private String dashscopeApiKey;

    @Bean
    Advisor appRagCloudAdvisor() {
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(
                        new DashScopeDocumentRetriever(
                                new DashScopeApi(dashscopeApiKey),
                                DashScopeDocumentRetrieverOptions.builder()
                                        .withIndexName("健身大师")
                                        .build()
                        )
                )
                .build();
    }

}
