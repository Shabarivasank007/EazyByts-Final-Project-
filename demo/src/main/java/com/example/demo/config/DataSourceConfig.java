package com.example.demo.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
@EnableConfigurationProperties(DataSourceProperties.class)
public class DataSourceConfig {

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        HikariConfig config = new HikariConfig();
        
        // Configure Hikari with the same properties as in application.properties
        config.setJdbcUrl(properties.determineUrl());
        config.setUsername(properties.determineUsername());
        config.setPassword(properties.determinePassword());
        config.setDriverClassName(properties.determineDriverClassName());
        
        // Set Hikari properties
        config.setPoolName("NewsHikariCP");
        config.setConnectionTimeout(20000);
        config.setMinimumIdle(5);
        config.setMaximumPoolSize(15);
        config.setIdleTimeout(300000);
        config.setMaxLifetime(1200000);
        config.setAutoCommit(true);
        config.setInitializationFailTimeout(1);
        config.setAllowPoolSuspension(true);
        
        return new HikariDataSource(config);
    }
}
