package com.frds;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import java.net.InetAddress;

@SpringBootApplication
@EnableCaching
@OpenAPIDefinition(
    info = @Info(
        title = "Firebird/RedDatabase REST Data Services",
        version = "1.0.0",
        description = "RESTful API для выполнения CRUD операций над сущностями БД и удаленного вызова процедур"
    )
)
public class FrdsApplication {
  @Value("${server.port:8080}")
  private String port;

  @Value("${server.servlet.context-path:/}")
  private String contextPath;

  public static void main(String[] args) {
    SpringApplication.run(FrdsApplication.class, args).getBean(FrdsApplication.class).printStartupInfo();
  }

  private void printStartupInfo() {
    try {
      String host = InetAddress.getLocalHost().getHostAddress();
      String baseUrl = "http://" + host + ":" + port + contextPath;

      System.out.println(" Firebird/RedDatabase REST Data Services v1.0.0");
      System.out.println("=".repeat(50));
      System.out.println("\n Доступные адреса:");
      System.out.println("   Swagger UI:        " + baseUrl + "swagger-ui/index.html");
      System.out.println("   OpenAPI JSON:      " + baseUrl + "v3/api-docs");
      System.out.println("   Endpoint к Meta API:   " + baseUrl + "meta/");
      System.out.println("   Endpoint к Entity API:   " + baseUrl + "api/");
      System.out.println("   Endpoint к Rpc API:   " + baseUrl + "rpc/");
      System.out.println("=".repeat(50));
    } catch (Exception e) {
      System.out.println("Приложение запущено");
      System.out.println("Swagger UI: http://localhost:" + port + contextPath + "swagger-ui/index.html");
    }
  }

}
