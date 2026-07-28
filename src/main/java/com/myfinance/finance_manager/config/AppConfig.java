package com.myfinance.finance_manager.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Finance Manager API")
                        .version("1.0.0")
                        .description("REST API for managing personal expenses")
                        .termsOfService("https://yourdomain.com/terms")
                        .license(new License().name("Apache 2.0").url("https://spring.io")));
    }
}
