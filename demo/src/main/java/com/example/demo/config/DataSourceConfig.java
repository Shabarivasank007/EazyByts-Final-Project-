package com.example.demo.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
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
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        
        // Set basic connection properties
        config.setJdbcUrl("jdbc:h2:mem:newsdb;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;DATABASE_TO_UPPER=false;CASE_INSENSITIVE_IDENTIFIERS=true");
        config.setUsername("sa");
        config.setPassword("");
        config.setDriverClassName("org.h2.Driver");
        
        // Set Hikari pool properties explicitly to avoid environment variable conflicts
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
