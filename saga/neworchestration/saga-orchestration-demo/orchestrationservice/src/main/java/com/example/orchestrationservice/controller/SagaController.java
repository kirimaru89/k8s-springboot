package com.example.orchestrationservice.controller;

import org.apache.camel.CamelExecutionException;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/saga")
public class SagaController {

    private static final Logger log = LoggerFactory.getLogger(SagaController.class);

    @Autowired
    private ProducerTemplate producerTemplate;

    @PostMapping("/start")
    public ResponseEntity<String> startSaga(@RequestBody String inputPayload) {
        log.info("Received request to start saga with payload: {}", inputPayload);
        try {
            // Send the payload to the Camel direct endpoint to start the saga
            Exchange exchange = producerTemplate.send("direct:startSagaOrchestration", ex -> {
                ex.getIn().setBody(inputPayload);
            });
            
            String result = exchange.getMessage().getBody(String.class);
            log.info("Saga processing finished. Result from orchestrator: {}", result);
            
            // Get the response code from exchange
            Integer responseCode = exchange.getMessage().getHeader("CamelHttpResponseCode", Integer.class);
            if (responseCode != null) {
                log.info("Saga execution failed with response code: {}", responseCode);
                return ResponseEntity.status(responseCode).body(result);
            }

            return ResponseEntity.ok(result);
        } catch (CamelExecutionException e) {
            // Extract the underlying cause if it's a CamelExecutionException
            Throwable cause = e.getCause();
            String errorMessage = (cause != null) ? cause.getMessage() : e.getMessage();
            log.error("Saga execution failed: {}", errorMessage, e);
            
            // Try to get the response code from the exception exchange
            Integer responseCode = null;
            if (e.getExchange() != null) {
                responseCode = e.getExchange().getMessage().getHeader("CamelHttpResponseCode", Integer.class);
            }
            
            // Check if the saga itself returned a specific error message in the body
            Object errorResponse = e.getExchange() != null ? e.getExchange().getMessage().getBody(String.class) : null;
            if (errorResponse instanceof String && !((String) errorResponse).isEmpty()) {
                return ResponseEntity.status(responseCode != null ? responseCode : HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body((String) errorResponse);
            }
            return ResponseEntity.status(responseCode != null ? responseCode : HttpStatus.INTERNAL_SERVER_ERROR.value())
                .body("Saga execution failed: " + errorMessage);
        } catch (Exception e) {
            log.error("Error starting saga: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to start saga: " + e.getMessage());
        }
    }
} 