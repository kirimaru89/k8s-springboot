package com.example.demo.config;

import org.slf4j.MDC;
import java.util.UUID;

public class TraceIdConfig {
    public static final String TRACE_ID = "traceId";

    public static void setTraceId() {
        MDC.put(TRACE_ID, generateTraceId());
    }

    public static void clearTraceId() {
        MDC.remove(TRACE_ID);
    }

    private static String generateTraceId() {
        return UUID.randomUUID().toString();
    }
} 