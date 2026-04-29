package com.example.lrucache;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class LruCacheApplication {

    public static void main(String[] args) {
        SpringApplication.run(LruCacheApplication.class, args);
    }

    /**
     * Declares the shared LRUCache instance as a Spring-managed singleton bean.
     * cache.capacity is read from application.properties.
     */
    @Bean
    public LRUCache<String, String> lruCache(
            @Value("${cache.capacity}") int capacity) {
        return new LRUCache<>(capacity);
    }
}
