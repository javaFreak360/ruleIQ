package com.highcourt.ruleIQ.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;

@Configuration
public class FileConfiguration {

    @Value("${file.producer.sftp.url}")
    private String sftpUrl;
    @Value("${file.producer.sftp.username}")
    private String username;
    @Value("${file.producer.sftp.password}")
    private String password;

        @Bean
        public DefaultSftpSessionFactory sftpSessionFactory() {
            DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory();
            factory.setHost(sftpUrl); // Replace with your host
            factory.setPort(22); // Default SFTP port
            factory.setUser(username); // Replace with your username
            factory.setPassword(password); // Replace with your password
            factory.setAllowUnknownKeys(true); // Set to false in production
            return factory;
        }
}
