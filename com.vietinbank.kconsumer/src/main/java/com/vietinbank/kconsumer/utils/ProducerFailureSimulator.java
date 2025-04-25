package com.vietinbank.kproducer.utils;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

public class ProducerFailureSimulator implements ProducerInterceptor<String, String> {
    private static final AtomicBoolean SIMULATE_FAILURE = new AtomicBoolean(false);
    
    public static void enableFailureSimulation() {
        SIMULATE_FAILURE.set(true);
    }
    
    public static void disableFailureSimulation() {
        SIMULATE_FAILURE.set(false);
    }

    @Override
    public ProducerRecord<String, String> onSend(ProducerRecord<String, String> record) {
        if (SIMULATE_FAILURE.get()) {
            throw new RuntimeException("Simulated producer failure");
        }
        return record;
    }

    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
        // No action needed
    }

    @Override
    public void close() {
        // No resources to clean up
    }

    @Override
    public void configure(Map<String, ?> configs) {
        // No configuration needed
    }
}