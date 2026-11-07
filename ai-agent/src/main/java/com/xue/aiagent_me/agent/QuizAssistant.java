package com.xue.aiagent_me.agent;

import com.xue.aiagent_me.advisor.MyLoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * 智慧答题助手，专注于辅助用户解答各类题目
 */
@Component
public class QuizAssistant extends ToolCallAgent {

    @Autowired
    public QuizAssistant(@Qualifier("allTools") ToolCallback[] allTools,
                         @Qualifier("dashscopeChatModel") ChatModel dashscopeChatModel) {
        super(allTools);

        // 基础配置
        this.setName("quizAssistant");
        this.setMaxSteps(15);
        this.setDuplicateThreshold(3);

        // 提示词设置
        this.setSystemPrompt(
                "你是一个专业的测评分析助手，擅长解读各类测试结果并提供个性化分析。你的目标是帮助用户理解测评结果、分析个人特点，" +
                        "并给出有针对性的建议和改进方向。你会根据测评类型（如性格测试、能力评估、情感测试等）调整分析策略，" +
                        "确保分析过程专业、全面且富有洞察力。你的回复应当条理清晰、直观易懂，必要时可以生成图文并茂的PDF报告。"
        );

        this.setNextStepPrompt(
                "请根据用户提供的测评内容，按以下步骤进行分析与反馈：\n" +
                        "1. 使用WebSearchTool搜索测评相关的专业知识和理论框架\n" +
                        "2. 使用ImageSearchTool搜索与分析结果相关的图片，以便可视化展示\n" +
                        "3. 使用DateTimeTool记录分析时间，增强报告的时效性\n" +
                        "4. 如需生成完整分析报告，使用PDFGenerationTool创建PDF文档，直接在内容中包含从ImageSearchTool获取的图片链接\n" +
                        "5. 对于需要保存的分析结果或中间数据，使用FileOperationTool保存\n" +
                        "6. 完成分析后，使用doTerminate工具结束当前交互"
        );

        // 初始化对话客户端
        this.setChatClient(
                ChatClient.builder(dashscopeChatModel)
                        .defaultAdvisors(new MyLoggerAdvisor())
                        .build()
        );
    }
}