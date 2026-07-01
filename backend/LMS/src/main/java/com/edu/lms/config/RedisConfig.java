package com.edu.lms.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching  // ← activates @Cacheable / @CacheEvict / @CachePut across the app
public class RedisConfig {

    // ── Cache name constants ──────────────────────────────────────────────────
    // Centralise them here so service classes import from one place,
    // not magic strings scattered everywhere.
    public static final String CACHE_COURSES   = "courses";   // list of all published
    public static final String CACHE_COURSE    = "course";    // single course by ID
    public static final String CACHE_DASHBOARD = "dashboard"; // admin overview stats
    public static final String CACHE_USERS     = "users";     // user profiles

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {

        // ── Serializer setup ──────────────────────────────────────────────────
        // GenericJackson2JsonRedisSerializer embeds @class in every value so Redis
        // knows which Java type to deserialize back to. This is required when
        // you store different types across caches (List<CourseDto> vs UserDto).
        ObjectMapper om = new ObjectMapper()
                .registerModule(new JavaTimeModule()) // handles LocalDateTime in UserDto
                .activateDefaultTyping(
                        LaissezFaireSubTypeValidator.instance,
                        ObjectMapper.DefaultTyping.NON_FINAL,
                        JsonTypeInfo.As.PROPERTY
                );

        JacksonJsonRedisSerializer valueSerializer =
                new JacksonJsonRedisSerializer(om.getClass());

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration
                .defaultCacheConfig()
                .disableCachingNullValues()            // never cache null → avoid negative cache bugs
                .serializeKeysWith(                    // keys stay as plain strings: "course::uuid-here"
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(                  // values stored as JSON
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(valueSerializer));

        // ── Per-cache TTL config ──────────────────────────────────────────────
        // WHY different TTLs?
        //   - courses/course: 10 min — changes on publish/edit, occasional eviction covers it
        //   - dashboard:       5 min — aggregate counts, slight stale is fine, shorter TTL for accuracy
        //   - users:          30 min — profiles change rarely (name, bio), long TTL is safe
        Map<String, RedisCacheConfiguration> perCacheConfig = new HashMap<>();

        perCacheConfig.put(CACHE_COURSES,
                defaultConfig.entryTtl(Duration.ofMinutes(10)));

        perCacheConfig.put(CACHE_COURSE,
                defaultConfig.entryTtl(Duration.ofMinutes(10)));

        perCacheConfig.put(CACHE_DASHBOARD,
                defaultConfig.entryTtl(Duration.ofMinutes(5)));

        perCacheConfig.put(CACHE_USERS,
                defaultConfig.entryTtl(Duration.ofMinutes(30)));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig.entryTtl(Duration.ofMinutes(10)))
                .withInitialCacheConfigurations(perCacheConfig)
                .build();
    }
}