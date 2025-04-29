package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import io.micrometer.context.ContextSnapshot;

@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean
    public TaskExecutor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("async-");
        // This wrapper makes the executor propagate the trace context
        executor.setTaskDecorator(runnable -> {
            return ContextSnapshot.captureAll().wrap(runnable);
        });
        
        return executor;
    }
}