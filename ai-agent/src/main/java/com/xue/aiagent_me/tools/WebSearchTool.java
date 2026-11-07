package com.xue.aiagent_me.tools;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class WebSearchTool {
    /**
    // SearchAPI搜索接口地址
    // SearchAPI搜索接口地址
    private static final String SEARCH_API_URL = "https://www.searchapi.io/api/v1/search";

    private final String apiKey;


    public WebSearchTool(String apiKey) {
        this.apiKey = apiKey;
    }

    @Tool(description = "Search for information from Baidu Search Engine")
    public String searchWeb(@ToolParam(description = "Search query keyword") String query) {
        // 构建请求参数
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("wd", query);

        paramMap.put("api_key", apiKey);
        paramMap.put("engine", "baidu");

        try {



            // 发送搜索请求
            String response = HttpUtil.get(SEARCH_API_URL, paramMap);

            // ========== 新增：打印完整响应（关键排查步骤） ==========
            System.out.println("SearchAPI.io 原始响应：" + response);
            // 解析返回结果
            JSONObject jsonObject = JSONUtil.parseObj(response);
            JSONArray organicResults = jsonObject.getJSONArray("organic_results");
            // 获取前5条结果并拼接
            return organicResults.stream().limit(5).map(obj -> ((JSONObject) obj).toString())
                    .collect(Collectors.joining(","));
        } catch (Exception e) {
            return "Error searching Baidu: " + e.getMessage();
        }


*/
    // 百度公开搜索接口（无密钥限制）
    private static final String BAIDU_SEARCH_URL = "https://www.baidu.com/s";

    // 构造器保留空参/带参均可（兼容原有代码）
    public WebSearchTool() {}
    public WebSearchTool(String apiKey) {} // 兼容原有调用逻辑，apiKey 无实际作用

    /**
     * 百度联网搜索（解析 HTML 获取前5条结果）
     */
    @Tool(description = "Search for information from Baidu Search Engine (no API key required)")
    public String searchWeb(@ToolParam(description = "Search query keyword") String query) {
        // 1. 构建百度搜索参数（标准参数：wd=关键词，rn=返回条数）
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("wd", query);   // 百度搜索核心参数（必须是 wd，不是 q）
        paramMap.put("rn", 5);      // 返回5条结果
        paramMap.put("ie", "utf-8"); // 编码格式
        paramMap.put("oe", "utf-8");

        try {
            // 2. 发送 GET 请求获取百度 HTML 响应
            String htmlResponse = HttpUtil.get(BAIDU_SEARCH_URL, paramMap);
            // 打印响应片段（排查用，可注释）
            System.out.println("百度响应片段：" + htmlResponse.substring(0, Math.min(htmlResponse.length(), 300)));

            // 3. Jsoup 解析 HTML，提取搜索结果
            Document doc = Jsoup.parse(htmlResponse);
            // 定位百度搜索结果核心节点（适配百度最新页面结构）
            Elements resultElements = doc.select("div.result-op.c-container, div.result-inner");

            // 4. 无结果处理
            if (resultElements.isEmpty()) {
                return "未找到「" + query + "」的相关搜索结果";
            }

            // 5. 提取前5条结果的标题 + 链接（易读格式）
            return resultElements.stream()
                    .limit(5)
                    .map(element -> {
                        // 提取标题
                        Element titleEle = element.selectFirst("h3 a");
                        String title = titleEle != null ? titleEle.text() : "无标题";
                        // 提取链接（处理百度跳转链接）
                        String link = titleEle != null ? titleEle.attr("href") : "无链接";
                        return "【" + title + "】\n链接：" + link;
                    })
                    .collect(Collectors.joining("\n\n"));

        } catch (Exception e) {
            return "百度搜索失败：" + e.getMessage();
        }
    }


}