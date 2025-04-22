package com.example.demo.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class KafkaRequestService {
    @Autowired
    private ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate;
    
    public String sendRequestAndGetReply(String request) {
        try {
            // send request
            ProducerRecord<String, String> record = new ProducerRecord<>("kRequests", "foo");
            RequestReplyFuture<String, String, String> replyFuture = replyingKafkaTemplate.sendAndReceive(record);

            // wait for send result
            SendResult<String, String> sendResult = replyFuture.getSendFuture().get(20, TimeUnit.SECONDS);
            System.out.println("Sent ok: " + sendResult.getRecordMetadata());

            // wait for reply
            ConsumerRecord<String, String> consumerRecord = replyFuture.get(20, TimeUnit.SECONDS);
            System.out.println("Return value: " + consumerRecord.value());

            return consumerRecord.value();
        } catch (Exception e) {
            log.error("Error in request/reply", e);
            throw new RuntimeException("Failed to get reply", e);
        }
    } 
} 