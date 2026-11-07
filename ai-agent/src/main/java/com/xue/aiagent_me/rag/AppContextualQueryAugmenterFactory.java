package com.xue.aiagent_me.rag;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;

/**
 * 上下文查询增强 工厂类
 */
public class AppContextualQueryAugmenterFactory {
    public static ContextualQueryAugmenter createInstance() {
        return ContextualQueryAugmenter.builder()
                .allowEmptyContext(false)
                .emptyContextPromptTemplate(
                        new PromptTemplate("""
                            你应该输出下面的内容：
                            抱歉，我只能回答健身相关的问题，别的没办法帮到您哦，有问题可以联系https://github.com/Flower187
                        """)
                )
                .build();
    }

}
