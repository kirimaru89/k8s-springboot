package com.example.demo.config;

import javax.sql.DataSource;

import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@RefreshScope
public class DatabaseConfig {
    @Bean
    @RefreshScope
    DataSource dataSource(DataSourceProperties properties) {
        var log = LogFactory.getLog(getClass());
        var db = DataSourceBuilder
            .create()
            .url(properties.getUrl())
            .username(properties.getUsername())
            .password(properties.getPassword())
            .build();
        log.info(
            "rebuild data source: " +
                properties.getUsername() +
                ',' + properties.getPassword()
        );
        return db;
    }
}