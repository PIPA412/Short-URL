package com.shortlink.framework.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson JSON serialization configuration.
 * <p>
 * Configures:
 * <ul>
 *   <li>Java 8 date/time module with Chinese timezone</li>
 *   <li>Long-to-String conversion (prevents JS precision loss for Snowflake IDs)</li>
 *   <li>Consistent date format: {@code yyyy-MM-dd HH:mm:ss}</li>
 * </ul>
 *
 * @author ShortLink
 */
@Configuration
public class JacksonConfig {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        // LocalDateTime serializer with Asia/Shanghai timezone
        javaTimeModule.addSerializer(LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_FORMAT)));

        // Snowflake Long IDs → String (prevents JavaScript precision loss)
        javaTimeModule.addSerializer(Long.class, ToStringSerializer.instance);
        javaTimeModule.addSerializer(Long.TYPE, ToStringSerializer.instance);

        return builder
                .modules(javaTimeModule)
                .simpleDateFormat(DATE_FORMAT)
                .build();
    }
}
