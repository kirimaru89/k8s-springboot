package com.example.demo.config;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("myCustomDltProcessor")
public class MyCustomDltProcessor {
    private static final Logger log = LoggerFactory.getLogger(MyCustomDltProcessor.class);

    /**
     * This method will be invoked when a message is sent to the DLT
     */
    public void processDltMessage(ConsumerRecord<?, ?> record, Exception exception) {
        try {
            // print transactionId
            String transactionId = "";
            String message = "";
            if (record.key() != null) {
                transactionId = record.key().toString();
            }
            if (record.value() != null) {
                message = record.value().toString();
            }

            log.info("🔥 DLT Processing: transactionId [{}] message [{}]", 
                    transactionId, message);

            log.error("🔥 DLT Processing: message [{}] failed with exception [{}]", 
                    transactionId, exception.getMessage(), exception);

            // ✅ Optionally:
            // - Store to a DB
            // - Trigger an alert
            // - Retry logic
            // - Save failed payload to S3 or fallback storage

            // Example of simple custom logic:
            if (exception.getMessage().contains("Deserialization")) {
                log.warn("Skipping due to deserialization failure: {}", record.value());
            } else {
                log.info("Storing to fallback system for analysis: {}", record.value());
                // call external service, persist to DB, etc.
            }
        } catch (Exception e) {
            log.error("💥 DLT handler itself threw an error! Failsafe fallback: {}", e.getMessage(), e);
        }
    }
}