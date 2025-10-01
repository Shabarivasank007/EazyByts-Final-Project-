package com.example.demo.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public HikariDataSource dataSource() {
        HikariConfig config = new HikariConfig();
        
        // Set default values
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
