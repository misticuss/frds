package com.frds.rest.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/**
 * @author Anton Zhulin
 * Date: 25.12.2025
 * Time: 14:49
 */
@Configuration
public class BasicAuthConfig {

  @Value("${spring.datasource.username}")
  private String dbUsername;

  @Value("${spring.datasource.password}")
  private String dbPassword;

  @Bean
  public UserDetailsService userDetailsService() {
    // Используем учетные данные из конфигурации БД
    return new InMemoryUserDetailsManager(
        User.withUsername(dbUsername)
            .password("{noop}" + dbPassword)  // {noop} = без шифрования
            .authorities("USER")
            .build()
    );
  }
}
