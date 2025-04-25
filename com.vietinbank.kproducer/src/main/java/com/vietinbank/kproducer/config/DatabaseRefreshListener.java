package com.vietinbank.kproducer.config;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.zaxxer.hikari.HikariDataSource;

@Component
public class DatabaseRefreshListener {
    private static final Logger log = LoggerFactory.getLogger(DatabaseRefreshListener.class);
    private final DataSource dataSource;

    @Autowired
    public DatabaseRefreshListener(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Listens for Spring Cloud Context refresh events (e.g., triggered by /actuator/refresh,
     * Vault config updates, or ContextRefresher) and evicts active connections from the HikariCP pool
     * to ensure the updated credentials from Vault take effect.
     */
    @EventListener(RefreshScopeRefreshedEvent.class)
    public void onRefresh() {
        // Log that a refresh event has been received and we're handling it
        log.info("RefreshScope event received - refreshing database connections");

        // Ensure the DataSource is a HikariDataSource (safe type check with Java 16+ pattern matching)
        if (dataSource instanceof HikariDataSource hikariDataSource) {

            // Only attempt eviction if the pool hasn't been closed
            if (!hikariDataSource.isClosed()) {

                // Soft evict all current connections in the pool:
                // - does not interrupt running queries
                // - marks connections to be replaced when returned
                hikariDataSource.getHikariPoolMXBean().softEvictConnections();

                // Confirm eviction action
                log.info("Database connections evicted to pick up new credentials");
            }
        }
    }
}