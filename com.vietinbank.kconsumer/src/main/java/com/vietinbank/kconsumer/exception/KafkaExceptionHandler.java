package com.vietinbank.kconsumer.exception;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;

public class KafkaExceptionHandler implements CommonErrorHandler {
    private static final Logger log = LoggerFactory.getLogger(KafkaExceptionHandler.class);
    
    @Override
    public boolean handleOne(Exception thrownException, ConsumerRecord<?, ?> record,
                          Consumer<?, ?> consumer, MessageListenerContainer container) {
        log.error("🔥 Kafka error - Topic={}, Partition={}, Offset={}, Exception={}",
                record.topic(), record.partition(), record.offset(), thrownException.getMessage(), thrownException);
        // Add recovery logic here (e.g., send to dead letter topic)

        return true;
    }
    
    @Override
    public void handleOtherException(Exception thrownException, Consumer<?, ?> consumer,
                                    MessageListenerContainer container, boolean batchListener) {
        log.error("Error in listener container: Container={}, Exception={}", 
                 container.getListenerId(), thrownException.getMessage(), thrownException);
    }
}