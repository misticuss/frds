package com.frds.rest.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Anton Zhulin
 * Date: 16.12.2025
 * Time: 17:08
 */

@Configuration
@EnableCaching
public class CacheConfig {

  @Bean
  public CacheManager cacheManager() {
    // Простой in-memory кэш на основе ConcurrentMap
    ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();

    // Опционально: задаем имена кэшей
    cacheManager.setCacheNames(java.util.List.of(
        "tables",
        "tableMetadata",
        "procedures",
        "columns"
    ));

    return cacheManager;
  }

  /**
   * Опционально: конфигурация для более продвинутого кэширования
   */
  /*@Bean
  public org.springframework.cache.interceptor.CacheResolver cacheResolver(CacheManager cacheManager) {
    return new org.springframework.cache.interceptor.SimpleCacheResolver(cacheManager);
  }*/
}
