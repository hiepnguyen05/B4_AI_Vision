package com.campus.security.aivision.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger Configuration
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI Vision Service API")
                        .version("1.0.0")
                        .description("AI Vision Service for Campus Security System - Product B4\n\n" +
                                "This service provides AI-powered image analysis capabilities, " +
                                "detecting objects such as persons, vehicles, helmets, and faces.")
                        .contact(new Contact()
                                .name("Group B4")
                                .email("groupb4@campus.edu"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8084/api")
                                .description("Development Server"),
                        new Server()
                                .url("http://ai-vision-service:8084/api")
                                .description("Docker Server")
                ));
    }
}
