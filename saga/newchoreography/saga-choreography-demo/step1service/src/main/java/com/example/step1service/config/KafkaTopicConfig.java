package com.example.step1service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    // Topics for step1service
    public static final String STEP1_COMPLETED_TOPIC = "step1-completed";
    public static final String STEP1_FAILED_TOPIC = "step1-failed";
    public static final String COMPENSATE_STEP1_TOPIC = "compensate-step1";

    // Topics for step2service interaction (produced by step1 or listened by step1 for compensation)
    public static final String START_STEP2_TOPIC = "start-step2"; // step1 -> step2
    public static final String STEP2_FAILED_FOR_STEP1_COMPENSATION_TOPIC = "step2-failed-for-step1-compensation"; // step2 -> step1 (when step2 fails and step1 needs to compensate)

    // General saga topics that step1 might listen to for broader coordination if needed (not strictly for its own compensation)
    public static final String SAGA_COMPLETED_TOPIC = "saga-completed";
    public static final String SAGA_FAILED_TOPIC = "saga-failed";

    @Bean
    public NewTopic step1CompletedTopic() {
        return TopicBuilder.name(STEP1_COMPLETED_TOPIC).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic step1FailedTopic() {
        return TopicBuilder.name(STEP1_FAILED_TOPIC).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic compensateStep1Topic() {
        return TopicBuilder.name(COMPENSATE_STEP1_TOPIC).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic startStep2Topic() {
        return TopicBuilder.name(START_STEP2_TOPIC).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic step2FailedForStep1CompensationTopic() {
        return TopicBuilder.name(STEP2_FAILED_FOR_STEP1_COMPENSATION_TOPIC).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic sagaCompletedTopic() {
        return TopicBuilder.name(SAGA_COMPLETED_TOPIC).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic sagaFailedTopic() {
        return TopicBuilder.name(SAGA_FAILED_TOPIC).partitions(1).replicas(1).build();
    }
} 