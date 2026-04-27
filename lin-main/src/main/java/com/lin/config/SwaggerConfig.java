package com.lin.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Increment 项目 API 接口文档")
                        .version("1.0")
                        .description("这是自动生成的 API 在线文档，基于 Spring Boot 3 + Knife4j"));
    }
}
