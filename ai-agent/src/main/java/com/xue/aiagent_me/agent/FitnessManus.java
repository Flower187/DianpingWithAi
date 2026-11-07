package com.xue.aiagent_me.agent;

import com.xue.aiagent_me.advisor.MyLoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

/**
 * 健身大师智能体
 */
@Component
public class FitnessManus extends ToolCallAgent {

    public FitnessManus(ToolCallback[] allTools, ChatModel dashscopeChatModel) {
        super(allTools);
        this.setName("fitnessManus");
        this.setSystemPrompt("""  
                You are YuManus, an all-capable AI assistant, aimed at solving any task presented by the user.
                You have various tools at your disposal that you can call upon to efficiently complete complex requests.
                """);
        this.setNextStepPrompt("""  
                Based on user needs, proactively select the most appropriate tool or combination of tools.
                For complex tasks, you can break down the problem and use different tools step by step to solve it.
                After using each tool, clearly explain the execution results and suggest the next steps.
                If you want to stop the interaction at any point, use the `terminate` tool/function call.
                """);
        this.setMaxSteps(20);
        this.setChatClient(
                ChatClient.builder(dashscopeChatModel)
                        .defaultAdvisors(new MyLoggerAdvisor())
                        .build()
        );
    }
}