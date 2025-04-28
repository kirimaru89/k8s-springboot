package com.vietinbank.kconsumer.services;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import static com.vietinbank.kconsumer.config.RabbitMQConfig.*;

@Service
public class RabbitMQConsumerService {

    @RabbitListener(queues = QUEUE_NAME)
    public void receiveMessage(String message) {
        System.out.println("📨 Received message: " + message);
    }
}