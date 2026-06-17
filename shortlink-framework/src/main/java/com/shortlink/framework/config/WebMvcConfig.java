package com.shortlink.framework.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration.
 * <p>
 * Configures static resource mappings and path matching.
 * Spring Boot 3.x uses {@code PathPatternParser} by default (ant_path_matcher
 * is deprecated), so no manual configuration is needed unless custom
 * interceptors are registered.
 *
 * @author ShortLink
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * Map Swagger UI / SpringDoc resources for API documentation access.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springdoc-openapi-ui/");
    }
}
