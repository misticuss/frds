package com.frds.rest.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.models.GroupedOpenApi;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

/**
 * @author Anton Zhulin
 * Date: 09.12.2025
 * Time: 14:46
 */
@Configuration
@SecurityScheme(
    name = "basicAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "basic",
    description = "Введите учетные данные БД"
)
public class OpenApiConfig {

  @Value("${app.api.openapi.title}")
  private String title;

  @Value("${app.api.openapi.description}")
  private String description;

  @Value("${app.api.openapi.version}")
  private String version;

  @Value("${server.servlet.context-path:/}")
  private String contextPath;

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title(title)
            .description(description)
            .version(version)
            .contact(new Contact()
                .name("FRDS Support")
                .email("support@example.com"))
            .license(new License()
                .name("Apache 2.0")
                .url("https://www.apache.org/licenses/LICENSE-2.0")))
        .servers(List.of(
            new Server()
                .url("http://localhost:8080" + contextPath)
                .description("Development Server")
        ));
  }

}
