package com.example.demo.config;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class VaultCredentialRefresher {
    private static final Logger log = LoggerFactory.getLogger(VaultCredentialRefresher.class);
    
    private final ContextRefresher contextRefresher;
    
    @Value("${spring.cloud.vault.kv.backend:secret}")
    private String vaultBackend;
    
    @Value("${spring.cloud.vault.kv.default-context:application}")
    private String vaultContext;
    
    @Autowired
    public VaultCredentialRefresher(ContextRefresher contextRefresher) {
        this.contextRefresher = contextRefresher;
    }
    
    @Scheduled(fixedDelayString = "${spring.cloud.vault.config.refresh-interval:30000}")
    public void refreshVaultCredentials() {
        log.info("Checking for Vault credential updates");
        Set<String> refreshedKeys = contextRefresher.refresh();
        
        if (!refreshedKeys.isEmpty()) {
            log.info("Refreshed configuration keys: {}", refreshedKeys);
        }
    }
}