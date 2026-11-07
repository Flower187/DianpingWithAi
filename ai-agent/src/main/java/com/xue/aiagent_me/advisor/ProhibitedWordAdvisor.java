package com.xue.aiagent_me.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 违禁词校验 Advisor
 * 检查用户输入是否包含违禁词
 */
@Component
@Slf4j
public class ProhibitedWordAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    private static final String DEFAULT_PROHIBITED_WORDS_FILE = "prohibited-words.txt";
    private final List<String> prohibitedWords;


    /**
     * 创建默认违禁词Advisor，从默认文件读取违禁词列表
     */
    public ProhibitedWordAdvisor() {
        this.prohibitedWords = loadProhibitedWordsFromFile(DEFAULT_PROHIBITED_WORDS_FILE);
       // log.info("初始化违禁词Advisor，违禁词数量: {}", prohibitedWords.size());
    }


    /**
     * 创建违禁词Advisor，从指定文件读取违禁词列表
     */
    public ProhibitedWordAdvisor(String prohibitedWordsFile) {
        this.prohibitedWords = loadProhibitedWordsFromFile(prohibitedWordsFile);
       // log.info("初始化违禁词Advisor，违禁词数量: {}", prohibitedWords.size());
    }

    /**
     * 创建违禁词Advisor，从指定文件读取违禁词列表
     */
    public List<String>loadProhibitedWordsFromFile(String filePath) {
        try {
            // 1. 加载文件资源（支持 classpath/绝对路径/相对路径）
            var resource = new ClassPathResource(filePath);
            var reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));


            // 3. 解析违禁词（按换行/逗号/空格分割，去重+过滤空行）
            List<String> words = reader.lines()
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .collect(Collectors.toList());

           // log.info("从文件 {} 加载违禁词 {} 个", filePath, words.size());
            return words;
        } catch (IOException e) {
           // log.error("加载违禁词文件失败！文件路径：{}", filePath, e);
            throw new RuntimeException("违禁词文件加载失败", e);
        }
    }

    /**
     * 检查请求中是否包含违禁词
     */
    private AdvisedRequest checkRequest(AdvisedRequest request) {
        String userText = request.userText();
        if (!StringUtils.hasText(userText)) {
            return request;
        }

        for (String prohibitedWord : prohibitedWords) {
            if (userText.contains(prohibitedWord)) {
               // log.warn("检测到违禁词：{}，原始请求：{}", prohibitedWord, userText);
                throw new ProhibitedWordException("您的输入包含违禁词：" + prohibitedWord + "，请修改后重新提交");
            }
        }

       // log.debug("请求通过违禁词检查：{}", userText);
        return request;
    }
    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        return chain.nextAroundCall(checkRequest(advisedRequest));
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        return chain.nextAroundStream(checkRequest(advisedRequest));
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return -100; // 确保在其他Advisor之前执行
    }
    /**
     * 违禁词异常
     */
    public static class ProhibitedWordException extends RuntimeException {
        public ProhibitedWordException(String message) {
            super(message);
        }
    }
}
