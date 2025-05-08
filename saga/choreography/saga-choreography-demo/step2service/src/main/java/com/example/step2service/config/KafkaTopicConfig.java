package com.example.step2service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    // Consumed by step2service
    public static final String START_STEP2_TOPIC = "start-step2"; // from step1service
    public static final String COMPENSATE_STEP2_TOPIC = "step3-failed-for-step2-compensation"; // from step3service or orchestrator

    // Produced by step2service
    public static final String STEP2_COMPLETED_TOPIC = "step2-completed";
    public static final String STEP2_FAILED_TOPIC = "step2-failed";
    public static final String START_STEP3_TOPIC = "start-step3"; // To trigger step3service
    public static final String STEP2_FAILED_FOR_STEP1_COMPENSATION_TOPIC = "step2-failed-for-step1-compensation"; // To trigger step1 compensation

    @Bean
    public NewTopic startStep2Topic() {
        return TopicBuilder.name(START_STEP2_TOPIC).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic compensateStep2Topic() {
        return TopicBuilder.name(COMPENSATE_STEP2_TOPIC).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic step2CompletedTopic() {
        return TopicBuilder.name(STEP2_COMPLETED_TOPIC).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic step2FailedTopic() {
        return TopicBuilder.name(STEP2_FAILED_TOPIC).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic startStep3Topic() {
        return TopicBuilder.name(START_STEP3_TOPIC).partitions(1).replicas(1).build();
    }
    @Bean
    public NewTopic step2FailedForStep1CompensationTopic() {
        return TopicBuilder.name(STEP2_FAILED_FOR_STEP1_COMPENSATION_TOPIC).partitions(1).replicas(1).build();
    }
} 