package com.shortlink.framework.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus configuration.
 * <p>
 * Registers the pagination interceptor and scans mapper interfaces
 * across all modules.
 *
 * @author ShortLink
 */
@Configuration
@MapperScan("com.shortlink.**.mapper")
public class MyBatisPlusConfig {

    /**
     * MyBatis-Plus interceptor chain.
     * Currently registers only the pagination interceptor.
     * Additional inner interceptors (e.g., optimistic locker, tenant)
     * can be added here.
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // Pagination plugin (supports MySQL, PostgreSQL, Oracle, etc.)
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        // Set max page size to prevent abuse: -1 = unlimited (controlled at service layer)
        paginationInterceptor.setMaxLimit(100L);
        interceptor.addInnerInterceptor(paginationInterceptor);

        return interceptor;
    }
}
