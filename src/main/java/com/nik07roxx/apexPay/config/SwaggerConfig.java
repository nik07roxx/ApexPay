package com.nik07roxx.apexPay.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

import java.util.List;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI myOpenAPI() {

        final String securitySchemeName = "bearerAuth";

        Contact contact = new Contact();
        contact.setEmail("developer@apexpay.com");
        contact.setName("ApexPay Team");
        contact.setUrl("https://www.apexpay.com");

        Info info = new Info()
                .title("ApexPay Fintech API")
                .version("1.0.0")
                .contact(contact)
                .description("This API exposes endpoints for managing bank accounts, processing transactions, and secure fund transfers.")
                .license(new License().name("Apache 2.0").url("http://springdoc.org"));

        Server server = new Server()
                .url("http://localhost:8080")
                .description("Testing");

        Components components = new Components()
                .addSecuritySchemes(securitySchemeName,
                        new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                );

        return new OpenAPI().info(info)
                .components(components)
                .servers(List.of(server)
                );
    }
}
