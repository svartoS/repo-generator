package com.svarto.repo_generator.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                              .title("Your API")
                              .version("1.0")
                              .description("https://www.youtube.com/watch?v=dQw4w9WgXcQ"));
    }
}
