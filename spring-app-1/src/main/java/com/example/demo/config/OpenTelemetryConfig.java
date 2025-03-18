package com.example.demo.config;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.semconv.ResourceAttributes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenTelemetryConfig {

    @Bean
    public OpenTelemetry openTelemetry(
            @Value("${opentelemetry.exporter.otlp.endpoint}") String otlpEndpoint,
            @Value("${spring.application.name:my-demo-app}") String serviceName,
            @Value("${management.tracing.sampling.probability:1.0}") double samplingProbability) {
        // Create resource with service name
        Resource resource = Resource.getDefault()
            .toBuilder()
            .put(ResourceAttributes.SERVICE_NAME, serviceName)
            .build();
            
        // Create OTLP exporter using the injected endpoint
        OtlpGrpcSpanExporter spanExporter = OtlpGrpcSpanExporter.builder()
            .setEndpoint(otlpEndpoint)
            .build();
            
        // Configure sampling rate
        io.opentelemetry.sdk.trace.samplers.Sampler sampler = 
            io.opentelemetry.sdk.trace.samplers.Sampler.traceIdRatioBased(samplingProbability);
        
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
            .setResource(resource)
            .setSampler(sampler)  // Set the sampler with configured probability
            .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
            .build();

        OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .build();

        GlobalOpenTelemetry.set(openTelemetry);
        return openTelemetry;
    }
}