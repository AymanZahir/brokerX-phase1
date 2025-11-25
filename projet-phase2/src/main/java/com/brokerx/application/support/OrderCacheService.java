package com.brokerx.application.support;

import java.util.List;
import java.util.UUID;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class OrderCacheService {

  private static final List<String> CACHE_NAMES = List.of("orderExecutions", "orderNotifications");

  private final CacheManager cacheManager;

  public OrderCacheService(CacheManager cacheManager) {
    this.cacheManager = cacheManager;
  }

  public void evictForOrder(UUID orderId) {
    CACHE_NAMES.forEach(
        cacheName -> {
          Cache cache = cacheManager.getCache(cacheName);
          if (cache != null) {
            cache.evict(orderId);
          }
        });
  }
}
