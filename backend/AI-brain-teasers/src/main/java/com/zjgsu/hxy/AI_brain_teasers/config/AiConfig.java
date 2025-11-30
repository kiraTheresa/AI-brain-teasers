package com.zjgsu.hxy.AI_brain_teasers.config;

import com.volcengine.ark.runtime.service.ArkService;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class AiConfig {

    @Value("${ai.apiKey}")
    String apiKey;

    @Bean
    public ArkService arkService() {
        ConnectionPool connectionPool = new ConnectionPool(5, 1, TimeUnit.SECONDS);
        Dispatcher dispatcher = new Dispatcher();

        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("未配置 ai.apiKey，无法调用火山方舟接口");
        }

        return ArkService.builder()
                .dispatcher(dispatcher)
                .connectionPool(connectionPool)
                .baseUrl("https://ark.cn-beijing.volces.com/api/v3/")
                .apiKey(apiKey)
                .build();
    }
}
