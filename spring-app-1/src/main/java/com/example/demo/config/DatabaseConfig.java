package com.example.demo.config;

import javax.sql.DataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseConfig {

    @Bean
    public DataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:otel:mysql://mysql:3306/book_db");
        dataSource.setDriverClassName("io.opentelemetry.instrumentation.jdbc.OpenTelemetryDriver");
        dataSource.setUsername("user");
        dataSource.setPassword("password");
        return dataSource;
    }
}