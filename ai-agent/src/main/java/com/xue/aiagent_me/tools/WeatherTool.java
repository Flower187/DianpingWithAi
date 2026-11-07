package com.xue.aiagent_me.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 天气查询工具
 */
public class WeatherTool {
    @Tool(description = "获取指定城市的当前天气情况")
    public String getWeather(@ToolParam(description = "城市名称") String city) {
        return city + "今天天气晴朗, 气温25°C";

    }
}
