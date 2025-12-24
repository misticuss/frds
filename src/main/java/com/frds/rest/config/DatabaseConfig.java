package com.frds.rest.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

/**
 * @author Anton Zhulin
 * Date: 16.12.2025
 * Time: 15:34
 */

@Configuration
public class DatabaseConfig {
  @Value("${spring.datasource.url}")
  private String jdbcUrl;

  @Value("${spring.datasource.username}")
  private String username;

  @Value("${spring.datasource.password}")
  private String password;

  @Value("${spring.datasource.driver-class-name}")
  private String driverClassName;

  @Value("${spring.datasource.hikari.connection-timeout:30000}")
  private long connectionTimeout;

  @Value("${spring.datasource.hikari.maximum-pool-size:10}")
  private int maximumPoolSize;

  @Value("${spring.datasource.hikari.minimum-idle:2}")
  private int minimumIdle;

  @Bean
  @Primary
  public DataSource dataSource() {
    HikariConfig config = new HikariConfig();

    // Основные настройки подключения
    config.setJdbcUrl(jdbcUrl);
    config.setUsername(username);
    config.setPassword(password);
    config.setDriverClassName(driverClassName);

    // Настройки пула
    config.setConnectionTimeout(connectionTimeout);
    config.setMaximumPoolSize(maximumPoolSize);
    config.setMinimumIdle(minimumIdle);
    config.setPoolName("Firebird-Hikari-Pool");

    // Опциональные настройки для Firebird
    config.addDataSourceProperty("charSet", "utf-8");
    config.addDataSourceProperty("lc_ctype", "UTF8");

    return new HikariDataSource(config);
  }

  @Bean
  public JdbcTemplate jdbcTemplate(DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }

  @Bean
  public NamedParameterJdbcTemplate namedParameterJdbcTemplate(DataSource dataSource) {
    return new NamedParameterJdbcTemplate(dataSource);
  }
}
