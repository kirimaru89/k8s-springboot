// package com.example.demo.config;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.cloud.context.refresh.ContextRefresher;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.vault.core.VaultTemplate;
// import org.springframework.vault.core.lease.SecretLeaseContainer;
// import org.springframework.vault.core.lease.domain.RequestedSecret;
// import org.springframework.vault.core.lease.event.SecretLeaseCreatedEvent;
// import org.springframework.vault.core.lease.event.SecretLeaseExpiredEvent;

// @Configuration
// public class VaultConfigWatcher {
//     private static final Logger log = LoggerFactory.getLogger(VaultConfigWatcher.class);
    
//     @Value("${spring.cloud.vault.kv.backend:secret}")
//     private String backend;
    
//     @Value("${spring.cloud.vault.kv.default-context:spring-app-1}")
//     private String context;
    
//     @Bean
//     public SecretLeaseContainer secretLeaseContainer(VaultTemplate vaultTemplate, 
//                                                     ContextRefresher contextRefresher) {
//         SecretLeaseContainer container = new SecretLeaseContainer(vaultTemplate);
        
//         String secretPath = backend + "/" + context;
//         log.info("Setting up secret lease monitoring for path: {}", secretPath);
        
//         // Register for KV secrets
//         container.addRequestedSecret(RequestedSecret.rotating(secretPath));
        
//         // Event listeners
//         container.addLeaseListener(event -> {
//             if (event instanceof SecretLeaseCreatedEvent) {
//                 log.info("Secret lease created/renewed for path: {}", secretPath);
//                 contextRefresher.refresh();
//             } else if (event instanceof SecretLeaseExpiredEvent) {
//                 log.info("Secret lease expired for path: {}", secretPath);
//             }
//         });
        
//         return container;
//     }
// }