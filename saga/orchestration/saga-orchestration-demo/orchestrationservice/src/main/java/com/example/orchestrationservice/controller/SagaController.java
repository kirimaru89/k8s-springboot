package com.example.orchestrationservice.controller;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
        log.info("Starting saga with payload: {}", inputPayload);
        
        Exchange exchange = producerTemplate.send("direct:startSagaOrchestration", ex -> {
            ex.getIn().setBody(inputPayload);
        });
        
        String result = exchange.getMessage().getBody(String.class);
        Integer responseCode = exchange.getMessage().getHeader("CamelHttpResponseCode", Integer.class);
        
        if (responseCode != null) {
            log.info("Saga completed with status code: {}", responseCode);
            return ResponseEntity.status(responseCode).body(result);
        }
        
        return ResponseEntity.ok(result);
    }
} 