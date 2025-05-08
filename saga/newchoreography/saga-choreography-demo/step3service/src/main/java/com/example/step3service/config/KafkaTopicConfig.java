package com.example.step3service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    // Consumed by step3service
    public static final String START_STEP3_TOPIC = "start-step3"; // from step2service
    public static final String COMPENSATE_STEP3_TOPIC = "compensate-step3"; // from orchestrator or on self-failure path recognition

    // Produced by step3service
    public static final String STEP3_COMPLETED_TOPIC = "step3-completed"; // Signals end of happy path for this branch
    public static final String STEP3_FAILED_TOPIC = "step3-failed";
    public static final String STEP3_FAILED_FOR_STEP2_COMPENSATION_TOPIC = "step3-failed-for-step2-compensation"; // To trigger step2 compensation

    // General Saga topics (step3 might produce to these)
    public static final String SAGA_COMPLETED_TOPIC = "saga-completed";
    public static final String SAGA_FAILED_TOPIC = "saga-failed";


    @Bean
    public NewTopic startStep3Topic() {
        return TopicBuilder.name(START_STEP3_TOPIC).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic compensateStep3Topic() {
        return TopicBuilder.name(COMPENSATE_STEP3_TOPIC).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic step3CompletedTopic() {
        return TopicBuilder.name(STEP3_COMPLETED_TOPIC).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic step3FailedTopic() {
        return TopicBuilder.name(STEP3_FAILED_TOPIC).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic step3FailedForStep2CompensationTopic() {
        return TopicBuilder.name(STEP3_FAILED_FOR_STEP2_COMPENSATION_TOPIC).partitions(1).replicas(1).build();
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