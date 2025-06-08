package com.example.project3.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI officeAppOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Office App API")
                        .description("API for Office Application with user management and presence tracking")
                        .version("v1.0.0")
                        .license(new License().name("Apache 2.0").url("https://springdoc.org")));
    }
}