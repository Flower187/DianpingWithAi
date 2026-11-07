package com.xue.aiagent_me.advisor;

import org.springframework.ai.chat.client.advisor.api.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

/**
 * 自定义 Re2 Advisor
 * 可提高大型语言模型的推理能力
 */
public class ReReadingAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    /**
     * 执行请求前，改写 Prompt
     *
     * @param advisedRequest
     * @return
     */
    private AdvisedRequest before(AdvisedRequest advisedRequest) {
// 1. 复制用户原始参数，避免修改原对象
        Map<String, Object> advisedUserParams = new HashMap<>(advisedRequest.userParams());
        // 2. 将用户原始提问存入参数Map，key为"re2_input_query"
        advisedUserParams.put("re2_input_query", advisedRequest.userText());
        // 3. 构建新的请求：用模板改写用户提问
        return AdvisedRequest.from(advisedRequest)
                .userText("""
                         Please carefully read and understand the following question:
                                   {re2_input_query}
                                   Before answering, confirm that you have fully understood all requirements in the question.
                        """)
                .userParams(advisedUserParams)
                .build();
    }

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        return chain.nextAroundCall(this.before(advisedRequest));
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        return chain.nextAroundStream(this.before(advisedRequest));
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }
}
