package com.vietinbank.kproducer.controllers;

import com.vietinbank.kproducer.services.LoggingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// import com.vietinbank.kproducer.services.KafkaProducerService;
import com.vietinbank.kproducer.services.LoggingService;
@RestController
@RequestMapping("/api/kafka-producer")
public class KafkaProducerController {
    @Autowired
    private LoggingService loggingService;
    
    // @Autowired
    // private KafkaProducerService kafkaProducerService;
    
    // @PostMapping("/send")
    // public String sendMessage(@RequestBody String message) {
    //     loggingService.logInfo("Received request to send message via Kafka: {}");
    //     kafkaProducerService.sendMessage(message);
    //     return "Message sent to Kafka!";
    // }
}