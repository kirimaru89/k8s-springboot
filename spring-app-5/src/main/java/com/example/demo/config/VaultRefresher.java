package com.example.demo.config;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.stereotype.Component;
import org.springframework.vault.core.lease.SecretLeaseContainer;
import org.springframework.vault.core.lease.event.SecretLeaseExpiredEvent;

@Component
public class VaultRefresher {
    public VaultRefresher(@Value("${spring.cloud.vault.database.role}") String databaseRole,
                  @Value("${spring.cloud.vault.database.backend}") String databaseBackend,
                  SecretLeaseContainer leaseContainer,
                  ContextRefresher contextRefresher) {

        final Log log = LogFactory.getLog(getClass());

        var vaultCredsPath = String.format("%s/creds/%s", databaseBackend, databaseRole);

        leaseContainer.addLeaseListener(event -> {
            if (vaultCredsPath.equals(event.getSource().getPath())) {
                if (event instanceof SecretLeaseExpiredEvent) {
                    contextRefresher.refresh();
                    log.info("refresh database credentials");
                }
            }
        });
    }
}