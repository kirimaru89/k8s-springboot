package com.example.demo.config;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenTelemetryConfig {

    @Bean
    public OpenTelemetry openTelemetry() {
        // Create OTLP exporter
        OtlpGrpcSpanExporter spanExporter = OtlpGrpcSpanExporter.builder()
            .setEndpoint("http://demo-opentelemetry-collector.monitoring.svc.cluster.local:4317")
            .build();
            
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
            .build();

        OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .build();

        GlobalOpenTelemetry.set(openTelemetry);
        return openTelemetry;
    }
}