package com.xue.aiagent_me.tools;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

/**
 * 天气查询工具配置类, 用于构造一个Function对象
 */
@Configuration
public class WeatherToolConfig {

    public record WeatherRequest(@JsonPropertyDescription("City name") String city) {
    }

    public record WeatherResponse(String result) {
    }
    @Bean
    @Description("Get current weather for a location")
    public Function<WeatherRequest, WeatherResponse> weatherTool() {
        return request -> new WeatherResponse(
                request.city() + "今天天气晴朗, 气温25°C"
        );
    }
}
