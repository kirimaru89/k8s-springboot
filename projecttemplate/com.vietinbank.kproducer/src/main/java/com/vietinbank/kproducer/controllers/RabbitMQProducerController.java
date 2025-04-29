package com.vietinbank.kproducer.controllers;

import com.vietinbank.kproducer.services.LoggingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vietinbank.kproducer.services.RabbitMQProducerService;
import com.vietinbank.kproducer.services.LoggingService;

@RestController
@RequestMapping("/api/rabbitmq-producer")
public class RabbitMQProducerController {
    @Autowired
    private RabbitMQProducerService rabbitMQProducerService;

    @PostMapping("/send")
    public String sendMessage(@RequestBody String message) {
        rabbitMQProducerService.sendMessage(message);
        return "Message sent to RabbitMQ: " + message;
    }
}