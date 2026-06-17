package com.shortlink.admin;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Servlet initializer for WAR deployment.
 * <p>
 * Extends {@link SpringBootServletInitializer} to configure the application
 * when deployed to an external servlet container (e.g., Tomcat).
 * Not used when running as a standalone JAR.
 *
 * @author ShortLink
 */
public class ServletInitializer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(ShortLinkApplication.class);
    }
}
