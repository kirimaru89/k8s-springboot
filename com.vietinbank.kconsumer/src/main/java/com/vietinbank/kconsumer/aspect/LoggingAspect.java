package com.vietinbank.kconsumer.aspect;

import com.vietinbank.kconsumer.services.LoggingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    private final LoggingService loggingService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LoggingAspect(LoggingService loggingService) {
        this.loggingService = loggingService;
    }

    @Around("execution(* com.vietinbank.kconsumer.controllers..*.*(..))")
    public Object logAroundController(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            String className = joinPoint.getSignature().getDeclaringTypeName();
            String methodName = joinPoint.getSignature().getName();

            // Thêm controller và method name vào MDC
            MDC.put("className", className);
            MDC.put("function", methodName);

            // Log request
            Object[] args = joinPoint.getArgs();
            loggingService.log("Start: ", args);

            Object result = joinPoint.proceed();

            // Log response cho các phương thức đồng bộ
            String responseData = result != null ? objectMapper.writeValueAsString(result) : "void";
            loggingService.log("Finish: ", responseData);
            
            return result;
        } finally {
            MDC.clear();
        }
    }

//    @Around("execution(* com.vietinbank.kconsumer.services..*.*(..))")
//    public Object logAroundService(ProceedingJoinPoint joinPoint) throws Throwable {
//        try {
//            String className = joinPoint.getSignature().getDeclaringTypeName();
//            String methodName = joinPoint.getSignature().getName();
//
//            // Thêm service và method name vào MDC
//            MDC.put("className", className);
//            MDC.put("function", methodName);
//
//            // Log request
//            Object[] args = joinPoint.getArgs();
//            loggingService.log("Start Service: {}.{}", className, methodName, args);
//
//            Object result = joinPoint.proceed();
//
//            // Log response cho các phương thức đồng bộ
//            String responseData = result != null ? objectMapper.writeValueAsString(result) : "void";
//            loggingService.log("Finish Service: {}.{}", className, methodName, responseData);
//
//            return result;
//        } finally {
//            MDC.clear();
//        }
//    }
}
