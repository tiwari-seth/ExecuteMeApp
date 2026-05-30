package com.executeme.common.web;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "ExecuteMe API",
                version = "v1",
                description = "Backend APIs for broker login, admin monitoring, positions, and async trade execution.",
                contact = @Contact(name = "ExecuteMe")
        ),
        tags = {
                @Tag(name = "Admin Monitoring", description = "Admin APIs for users and broker sessions"),
                @Tag(name = "Trade Execution", description = "Admin APIs for trade requests and execution jobs"),
                @Tag(name = "Positions", description = "Admin APIs for broker positions"),
                @Tag(name = "Broker OAuth", description = "Public broker login callback APIs")
        }
)
@SecurityScheme(
        name = "basicAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "basic",
        description = "HTTP Basic authentication using ADMIN_USERNAME and ADMIN_PASSWORD."
)
public class OpenApiConfig {
}
