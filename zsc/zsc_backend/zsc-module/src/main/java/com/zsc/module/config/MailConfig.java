package com.zsc.module.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * 邮件配置类
 */
@Configuration
public class MailConfig {

    @Bean
    public JavaMailSender mailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        
        // 配置邮件服务器信息（这些值应该从配置文件中读取）
        mailSender.setHost("smtp.qq.com"); // 默认使用 QQ 邮箱 SMTP 服务器
        mailSender.setPort(587);
        mailSender.setUsername("your-email@qq.com"); // 默认占位符，需要在配置文件中覆盖
        mailSender.setPassword("your-auth-code"); // 默认占位符，需要在配置文件中覆盖
        
        // 配置 SMTP 属性
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");
        
        return mailSender;
    }
}
