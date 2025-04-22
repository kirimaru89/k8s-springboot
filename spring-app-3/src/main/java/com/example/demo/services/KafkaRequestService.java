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
import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Slf4j
@Service
public class KafkaRequestService {
    @Autowired
    private ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate;
    
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    public String sendRequestAndGetReply(String request) {
        try {
            return sendRequestAndGetReplyInternal(request);
        } catch (Exception e) {
            log.error("Error in request/reply", e);
            throw new RuntimeException("Failed to get reply", e);
        }
    }

    public CompletableFuture<String> sendRequestAndGetReplyAsync(String request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ProducerRecord<String, String> record = new ProducerRecord<>("kRequests", request);
                RequestReplyFuture<String, String, String> replyFuture = replyingKafkaTemplate.sendAndReceive(record);
                
                // Convert the blocking operations to CompletableFuture
                CompletableFuture<SendResult<String, String>> sendFuture = new CompletableFuture<>();
                CompletableFuture<ConsumerRecord<String, String>> replyFuture2 = new CompletableFuture<>();
                
                // Handle send result asynchronously
                replyFuture.getSendFuture().whenComplete((result, ex) -> {
                    if ("error_simulation".equalsIgnoreCase(request)) {
                        sendFuture.completeExceptionally(new RuntimeException("Error simulation"));
                    } else if (ex != null) {
                        sendFuture.completeExceptionally(ex);
                    } else {
                        sendFuture.complete(result);
                    }
                });
                
                // Handle reply asynchronously
                CompletableFuture.runAsync(() -> {
                    try {
                        ConsumerRecord<String, String> consumerRecord = replyFuture.get(20, TimeUnit.SECONDS);
                        replyFuture2.complete(consumerRecord);
                    } catch (Exception e) {
                        replyFuture2.completeExceptionally(e);
                    }
                }, taskExecutor);
                
                // Wait for both operations to complete
                return CompletableFuture.allOf(sendFuture, replyFuture2)
                    .thenApply(v -> {
                        try {
                            log.info("Sent ok: {}", sendFuture.get().getRecordMetadata());
                            String reply = replyFuture2.get().value();
                            log.info("Return value: {}", reply);
                            return reply;
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to process reply", e);
                        }
                    })
                    .get(20, TimeUnit.SECONDS);
                    
            } catch (Exception e) {
                log.error("Error in async request/reply", e);
                throw new RuntimeException("Failed to get reply", e);
            }
        }, taskExecutor);
    }

    private String sendRequestAndGetReplyInternal(String request) throws Exception {
        // send request
        ProducerRecord<String, String> record = new ProducerRecord<>("kRequests", request);
        RequestReplyFuture<String, String, String> replyFuture = replyingKafkaTemplate.sendAndReceive(record);

        // wait for send result
        SendResult<String, String> sendResult = replyFuture.getSendFuture().get(20, TimeUnit.SECONDS);
        log.info("Sent ok: {}", sendResult.getRecordMetadata());

        // wait for reply
        ConsumerRecord<String, String> consumerRecord = replyFuture.get(20, TimeUnit.SECONDS);
        log.info("Return value: {}", consumerRecord.value());

        return consumerRecord.value();
    }
} 