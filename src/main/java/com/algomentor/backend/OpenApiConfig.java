package com.algomentor.backend;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AlgoMentor API")
                        .version("1.0.0")
                        .description("Professional algorithm solution analysis with LLM-powered feedback and interactive mentoring.")
                        .contact(new Contact()
                                .name("Elchibek Dastanov")
                                .url("https://github.com/elchibekdastanov/algomentor"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")));
    }
}

