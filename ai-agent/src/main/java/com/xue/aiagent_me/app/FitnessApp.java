package com.xue.aiagent_me.app;

import cn.hutool.json.JSONUtil;
import com.xue.aiagent_me.advisor.MyLoggerAdvisor;
import com.xue.aiagent_me.chatmemory.FileBasedChatMemory;
import com.xue.aiagent_me.chatmemory.MySQLChatMemory;
import com.xue.aiagent_me.chatmemory.MybatisPlusChatMemory;
import com.xue.aiagent_me.rag.AppRagCustomAdvisorFactory;
import com.xue.aiagent_me.rag.QueryRewriter;
import com.xue.aiagent_me.service.ChatMemoryService;
import dev.langchain4j.agent.tool.P;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import com.xue.aiagent_me.advisor.ProhibitedWordAdvisor;

import javax.sql.DataSource;
import java.util.List;

/**
 * 健身App
 */
@Component
@Slf4j
public class FitnessApp {

    private static final String SYSTEM_PROMPT = """
        系统角色设定
            你是一位拥有10年经验的资深健身教练与运动康复专家，持有NSCA-CSCS、NASM-CES等国际认证。你的核心目标是：
                1️. 通过结构化引导式对话全面了解用户健康状况
                2. 提供科学安全的运动康复方案与健身计划
                3. 培养用户长期健康管理意识
            
        核心行为准则
            1. 安全优先：始终以"无评估，不训练"为原则，拒绝未掌握充分信息时的建议
            2. 主动倾听：通过开放式提问挖掘用户潜在需求（如："您提到的肩痛是否影响日常穿衣动作？"）
            3. 分层引导：按「基础信息→核心诉求→限制因素→目标设定」四阶推进对话
                
        对话流程指令
            第一阶段：建立信任与收集基础信息
                开场白："您好！我是您的专属健康管家[AI名称]。在制定方案前，我需要先了解您的身体状况。您方便用30秒描述最近一次运动后的身体感受吗？"
                关键问题：
                    受伤史："过去12个月是否经历过需要停训超过3天的伤病？具体部位和康复时长是？"
                    疼痛评估："请用1-10分评价您当前主要不适部位（如膝盖）的疼痛程度，并描述是刺痛/钝痛/酸痛？"
                    生活习惯："您每日久坐时长是否超过6小时？通常选择什么通勤方式？"
            第二阶段：需求深度挖掘
                痛点聚焦："如果只能解决一个健康问题，您最想改善的是______（体态/疼痛/体能/体重）？"
                目标拆解：
                    运动康复："这个肩痛是否在过顶动作（如晾衣服）时加重？晨起是否有僵硬感？"
                    增肌需求："您希望重点加强的肌群是？过去尝试过哪些训练方式？"
                    减脂需求："您更倾向通过调整饮食结构还是增加有氧运动来实现目标？"
            第三阶段：方案制定与风险规避
                安全筛查：
                    "您最近3个月是否出现过以下情况：胸痛？严重眩晕？关节肿胀？"
                    "是否正在服用可能影响心率的药物？（如β受体阻滞剂）"
                方案生成逻辑：
                    1. 根据用户回答自动匹配禁忌动作库（如腰椎间盘突出患者禁用卷腹）
                    2. 生成阶梯式计划：
                        急性期（0-2周）：以筋膜松解+等长收缩为主
                        恢复期（3-6周）：加入离心控制训练
                        强化期（7周+）：渐进式抗阻训练
            第四阶段：行为干预与教育
                知识植入：
                    "您知道吗？每久坐1小时，建议进行3分钟「微型训练」：踮脚尖×30s+靠墙静蹲×30s"
                    "疼痛缓解的黄金法则：冰敷每次不超过20分钟，间隔至少90分钟"
                持续跟进：
                    "下周同一时间，我会询问您这三个指标变化：①疼痛频率 ②活动度 ③训练完成质量"
                    "建议您记录「疼痛日记」，包含：动作、时间、强度、缓解方式"
        特殊场景处理:
            医疗红线：当用户出现以下表述时立即终止建议并引导就医：
                "最近咳嗽时会有尿液渗出"（可能涉及盆底肌功能障碍）
                "夜间常被小腿抽筋痛醒"（需排查血管/神经问题）
        话术示例："根据您的描述，这种足跟痛可能涉及足底筋膜炎。我为您准备了3个居家缓解动作，但建议您在症状持续超过2周时进行超声检查确诊。"
                
        技术实现建议
            1. 构建「症状-动作」关联数据库，实时过滤高危动作
            2. 集成体态评估AI模块，支持用户上传侧面/背面照片进行初步筛查
            3️. 设计训练计划的可视化进度条，包含「适应性」「强度」「恢复」三维指标
    """;

    /**
     * 对话客户端
     */
    private final ChatClient chatClient;
    /**
     * CharMemory持久化到本地文件的存放路径
     */


    public FitnessApp(ChatModel dashscopeChatModel , MybatisPlusChatMemory chatMemory) {
        //  String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
        //  ChatMemory chatMemory = new FileBasedChatMemory(fileDir);
        //ChatMemory chatMemory = new InMemoryChatMemory();
       //  ChatMemory chatMemory = new MySQLChatMemory(dataSource);



        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        new MyLoggerAdvisor()
                        /**
                         * 1.kryo文件读取持久化
                        new MessageChatMemoryAdvisor(
                                new FileBasedChatMemory(CHAT_MEMORY_PATH)
                        ),

                             2.敏感词拦截器
                         new ProhibitedWordAdvisor()
                         */


                )
                .defaultAdvisors(
                        advisorSpec -> advisorSpec.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10)
                )
                .build();
    }

    public String doChat(String message, String chatId) {
        ChatResponse chatResponse = chatClient.prompt()
                .user(message)
                .advisors(
                        advisorSpec -> advisorSpec.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                )
                .call()
                .chatResponse();
        String outputText = chatResponse.getResult().getOutput().getText();
        log.info("AI output: {}", outputText);
        return outputText;
    }




    // 定义 AI 输出的数据模型 FitnessReport(结构化)
    public record FitnessReport(String title, List<String> suggestings) {
    }
    public FitnessReport doChatWithReport(String message, String chatId) {
        FitnessReport fitnessReport=chatClient.prompt().system("每次对话后都要生成健身结果，标题为{用户名}的健身报告，内容为建议列表")
                .user(message)
                // 3. 关联对话上下文：通过chatId找到该用户的历史对话，保证建议的连贯性
                .advisors(
                        advisorSpec -> advisorSpec.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                )
                // 4. 调用AI接口，自动将返回结果转换成FitnessReport对象（结构化）
                .call()
                .entity(FitnessReport.class);

        log.info("fitnessReport:{}", JSONUtil.toJsonStr(fitnessReport));
        return fitnessReport;
    }



    @Resource(name = "appVectorStore")
    private VectorStore appVectorStore;
    @Resource
    private Advisor appRagCloudAdvisor;
    @Resource
    private QueryRewriter queryRewriter;

    @Resource(name = "vectorStore")
    private VectorStore pgVectorVectorStore;

//RAG ：Springai+本地库
    public String doChatWithRag(String message, String chatId) {
        //重写用户输入message的文本
        String rewrittenMessage = queryRewriter.doQueryRewrite(message);
        ChatResponse chatResponse = chatClient.prompt().user(rewrittenMessage)
                .advisors(advisorSpec -> advisorSpec.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY,chatId))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                // 应用知识库问答
                .advisors(new QuestionAnswerAdvisor(appVectorStore))
               //RAG：Springai+云数据库  应用增强检索服务（云知识库服务）
 //             .advisors(appRagCloudAdvisor)
               //基于 PostgreSQL vector 实现向量数据库
//               .advisors(new QuestionAnswerAdvisor(pgVectorVectorStore))
                // 应用自定义的 RAG 检索增强服务（文档查询器 + 上下文增强器）
             .advisors(
                       AppRagCustomAdvisorFactory.createAppRagCustomAdvisor(appVectorStore, "单身")
             )

                .call()
                .chatResponse();



        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;

    }


    @Resource
    private ToolCallback[] allTools;

    public String doChatWithTools(String message, String chatId) {
        ChatResponse chatResponse = chatClient.prompt()
                .user(message)
                .advisors(
                        advisorSpec -> advisorSpec.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                )
                .tools(allTools)
                .call()
                .chatResponse();
        String outputText = chatResponse.getResult().getOutput().getText();
        return outputText;
    }




    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    public String doChatWithMcp(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(
                        advisorSpec -> advisorSpec.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                )
                .tools(toolCallbackProvider)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }


}
