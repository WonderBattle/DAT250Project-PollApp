package com.DAT250Project.PollApp.CacheConfig;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    // Injects Redis host from application.properties, defaults to "localhost"
    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    // Injects Redis port from application.properties, defaults to 6379
    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

//TODO need to decide to use it or not
    // Injects Redis username (optional, for Redis 6+ ACL)
    @Value("${spring.data.redis.username:}")
    private String redisUsername;

    // Injects Redis password (optional)
    @Value("${spring.data.redis.password:}")
    private String redisPassword;
//-----------------------------------------------------

    // Injects Redis database index, defaults to 0
    @Value("${spring.data.redis.database:0}")
    private int redisDatabase;

    // Creates a bean for Redis connection factory
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // Creates configuration for standalone Redis server
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        // Sets the Redis host
        config.setHostName(redisHost);
        // Sets the Redis port
        config.setPort(redisPort);
        // Sets the Redis database index
        config.setDatabase(redisDatabase);

        //todo If password is provided, set authentication
        if (!redisPassword.isEmpty()) {
            // For Redis 6+ with ACL, set username if provided
            if (!redisUsername.isEmpty()) {
                // Redis 6+ with ACL
                config.setUsername(redisUsername);
            }
            // Set the password
            config.setPassword(redisPassword);
        }

        // Creates and returns Lettuce connection factory (high-performance Redis client)
        return new LettuceConnectionFactory(config);
    }

    // Creates a RedisTemplate bean for Redis operations
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        // Creates RedisTemplate instance for String keys and Object values
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // Sets the connection factory
        template.setConnectionFactory(redisConnectionFactory());

        // Serializer for keys (uses String serializer)
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Serializer for values (uses JSON serializer for complex objects)
        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        // Initializes the template after properties are set
        template.afterPropertiesSet();
        return template;
    }

    // Creates the CacheManager bean for Spring caching abstraction
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                // Sets default Time-To-Live to 10 minutes
                .entryTtl(Duration.ofMinutes(10))
                // Disables caching of null values
                .disableCachingNullValues()
                // Sets key serializer to String
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                // Sets value serializer to JSON
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()));

        // Map for cache-specific configurations
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Poll results cache with 5 minutes TTL (shorter because results change frequently)
        cacheConfigurations.put("pollResults",
                defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // Users cache with 30 minutes TTL (longer because user data changes less frequently)
        cacheConfigurations.put("users",
                defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Builds and returns the RedisCacheManager
        return RedisCacheManager.builder(connectionFactory)
                // Sets default configuration
                .cacheDefaults(defaultConfig)
                // Sets cache-specific configurations
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
