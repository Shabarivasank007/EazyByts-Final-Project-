package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.context.ConfigurableApplicationContext;
import javax.sql.DataSource;
import com.zaxxer.hikari.HikariDataSource;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableAsync
@EnableTransactionManagement
public class NewsplatformApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(NewsplatformApplication.class, args);

        // Add shutdown hook for clean database shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                DataSource dataSource = context.getBean(DataSource.class);
                if (dataSource instanceof HikariDataSource) {
                    ((HikariDataSource) dataSource).close();
                }
            } catch (Exception e) {
                System.err.println("Error during database shutdown: " + e.getMessage());
            }
        }));

        String banner = """
            \u001B[36m============================================================
            \u001B[1m🚀 \u001B[32mNews Reading Platform Started Successfully!\u001B[0m
            📰 \u001B[33mAccess the application at: \u001B[34mhttp://localhost:8080\u001B[0m
            🔧 \u001B[33mH2 Console available at: \u001B[34mhttp://localhost:8080/h2-console\u001B[0m
            📊 \u001B[33mActuator endpoints: \u001B[34mhttp://localhost:8080/actuator\u001B[0m
            \u001B[36m============================================================\u001B[0m
            """;
        System.out.println(banner);
    }
}
