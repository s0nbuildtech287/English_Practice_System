package com.smarteng.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI smartEngOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SmartEnglish — Interactive Read & Listen API")
                        .description("Backend RESTful services for CEFR Articles, Neural TTS Audio, Instant Multi-tier Dictionary, and 3D SRS Flashcards.")
                        .version("v1.0.0")
                        .contact(new Contact().name("Xuan Son (xu4ns0n)").url("https://github.com/s0nbuildtech287"))
                        .license(new License().name("Apache 2.0").url("https://springdoc.org")));
    }
}
