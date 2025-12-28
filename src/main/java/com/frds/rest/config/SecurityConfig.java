package com.frds.rest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * @author Anton Zhulin
 * Date: 25.12.2025
 * Time: 14:42
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            // ВСЕ ресурсы Swagger UI - публичные
            .requestMatchers(
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/swagger-ui.html",
                "/webjars/**",
                "/swagger-resources/**",
                "/meta/"
            ).permitAll()

            // API endpoint'ы требуют авторизации
            .requestMatchers("/api/**").authenticated()
            .requestMatchers("/rpc/**").authenticated()
            .requestMatchers("/meta/**").authenticated()

            // Все остальное - публичное или по необходимости
            .anyRequest().permitAll()
        )
        .httpBasic(httpBasic -> httpBasic
            .authenticationEntryPoint((request, response, authException) -> {
              // Кастомный ответ для API endpoint'ов
              if (request.getRequestURI().startsWith("/api/") ||
                  request.getRequestURI().startsWith("/rpc/") ||
                  request.getRequestURI().startsWith("/meta/")) {
                response.setStatus(401);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(
                    "{\"status\": 401,\"message\": \"Неверные учетные данные\"}"
                );
              } else {
                // Для остальных - стандартный ответ
                response.sendError(401, "Unauthorized");
              }
            })
        );

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.addAllowedOriginPattern("*");

    configuration.setAllowedMethods(List.of(
        "GET", "POST", "PUT", "DELETE", "PATCH"
    ));

    configuration.setAllowedHeaders(List.of("*"));

    configuration.setExposedHeaders(List.of("*"));

    configuration.setAllowCredentials(true);

    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);

    return source;
  }
}
