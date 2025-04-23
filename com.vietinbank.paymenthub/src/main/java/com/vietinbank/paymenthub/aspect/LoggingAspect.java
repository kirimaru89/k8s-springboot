package com.vietinbank.paymenthub.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import com.vietinbank.paymenthub.services.LoggingService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Aspect
@Component
public class LoggingAspect {
    private final LoggingService loggingService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LoggingAspect(LoggingService loggingService) {
        this.loggingService = loggingService;
    }

    @Around("execution(* com.vietinbank.paymenthub.controllers..*.*(..))")
    public Object logAroundController(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            String className = joinPoint.getSignature().getDeclaringTypeName();
            String methodName = joinPoint.getSignature().getName();

            // Thêm controller và method name vào MDC
            MDC.put("className", className);
            MDC.put("function", methodName);
            // System.out.println("logAroundController add to MDC: " + MDC.get("className") + " " + MDC.get("function"));

            // Log request
            Object[] args = joinPoint.getArgs();
            loggingService.log("Start: ", args);

            Object result = joinPoint.proceed();

            // Log response cho các phương thức đồng bộ
            String responseData = result != null ? objectMapper.writeValueAsString(result) : "void";
            loggingService.log("Finish: ", responseData);

            return result;
        } finally {
            MDC.remove("className");
            MDC.remove("function");
        }
    }

    @Around("execution(* com.vietinbank.paymenthub.services..*.*(..)) " +
           "&& !execution(* com.vietinbank.paymenthub.services.LoggingService.*(..))")
    public Object logAroundService(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            String className = joinPoint.getSignature().getDeclaringTypeName();
            String methodName = joinPoint.getSignature().getName();

            // Thêm service và method name vào MDC
            MDC.put("className", className);
            MDC.put("function", methodName);
            // System.out.println("logAroundService add to MDC: " + MDC.get("className") + " " + MDC.get("function"));

            // Log request
            Object[] args = joinPoint.getArgs();
            loggingService.log("Start Service:", args);

            Object result = joinPoint.proceed();

            // Log response cho các phương thức đồng bộ
            String responseData = result != null ? objectMapper.writeValueAsString(result) : "void";
            loggingService.log("Finish Service: ", responseData);

            return result;
        } finally {
            // MDC.remove("className");
            // MDC.remove("function");
            // System.out.println("logAroundService MDC cleared: " + MDC.get("className") + " " + MDC.get("function"));
        }
    }
}
