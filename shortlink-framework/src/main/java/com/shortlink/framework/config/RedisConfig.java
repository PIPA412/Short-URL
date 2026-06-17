package com.shortlink.framework.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Redis configuration.
 * <p>
 * Provides:
 * <ul>
 *   <li>{@link RedisTemplate} with GenericJackson2Json serialization
 *       for complex object storage (safe polymorphic handling).</li>
 *   <li>{@link StringRedisTemplate} for simple key-value operations
 *       (short code → URL mapping, counters, locks).</li>
 * </ul>
 * <p>
 * <b>Serialization notes:</b>
 * The {@link GenericJackson2JsonRedisSerializer} writes fully-qualified
 * type metadata into the JSON, enabling safe deserialization without
 * the {@code activateDefaultTyping} vulnerability.
 *
 * @author ShortLink
 */
@Configuration
public class RedisConfig {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * Shared ObjectMapper used by both Redis serializers.
     * Configures Java 8 time module support and visibility.
     */
    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Java 8 date/time support (LocalDateTime, etc.)
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_FORMAT)));
        javaTimeModule.addDeserializer(LocalDateTime.class,
                new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATE_FORMAT)));
        mapper.registerModule(javaTimeModule);

        // Use field-level visibility (no need for getters/setters)
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        // DO NOT use activateDefaultTyping — it opens CVE-2019-12384 vector.
        // GenericJackson2JsonRedisSerializer handles type metadata safely.

        return mapper;
    }

    /**
     * RedisTemplate for storing complex Java objects as JSON.
     * Uses GenericJackson2JsonRedisSerializer for safe polymorphic serialization.
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // String serializer for keys
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // GenericJackson2Json Redis serializer — safe type handling
        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(redisObjectMapper());

        // Set serializers
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * StringRedisTemplate for lightweight key-value operations.
     * Used for: short code → URL mapping, click counters, distributed locks.
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(
            RedisConnectionFactory connectionFactory) {
        StringRedisTemplate stringTemplate = new StringRedisTemplate();
        stringTemplate.setConnectionFactory(connectionFactory);

        // Use the same string serializer for consistency
        stringTemplate.setKeySerializer(RedisSerializer.string());
        stringTemplate.setValueSerializer(RedisSerializer.string());
        stringTemplate.setHashKeySerializer(RedisSerializer.string());
        stringTemplate.setHashValueSerializer(RedisSerializer.string());

        stringTemplate.afterPropertiesSet();
        return stringTemplate;
    }
}
