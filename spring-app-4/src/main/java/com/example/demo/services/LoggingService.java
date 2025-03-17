package com.example.demo.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.zalando.logbook.BodyFilter;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

@Service
public class LoggingService {

    private static final Logger logger = LoggerFactory.getLogger(LoggingService.class);
    
    @Autowired
    private BodyFilter bodyFilter;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void log(String message, Object... args) {
        if (args != null && args.length > 0) {
            try {
                String jsonString = objectMapper.writeValueAsString(args);
                // log header
                String maskedData = bodyFilter.filter("application/json", jsonString);
                logger.info(message + " - Data: {}", maskedData);
            } catch (JsonProcessingException e) {
                logger.error("Error masking sensitive data", e);
                logger.info(message);
            }
        } else {
            logger.info(message);
        }
    }

    public void logInfo(String message) {
        logger.info(message);
    }

    public void logError(String message) {
        logger.error(message);
    }

    public void logWarn(String message) {
        logger.warn(message);
    }

    public void logDebug(String message) {
        logger.debug(message);
    }
}
