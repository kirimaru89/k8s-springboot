package com.example.demo.services;

import com.example.demo.adapter.kafka.producer.KafkaProducer;
import com.example.demo.dto.request.transaction.CreateTransactionRequestDto;
import com.example.demo.dto.response.account.ListTransactionsResponseDto;
import com.example.demo.dto.response.account.TransactionResponseDto;
import com.example.demo.enums.TransactionStatus;
import com.example.demo.models.Transaction;
import com.example.demo.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.demo.mapper.TransactionMapper.*;

@Service
public class TransactionService {
    @Value("${spring.kafka.producer.topic.transaction.initiated}")
    private String transactionInitiatedTopic;

    private final KafkaProducer kafkaProducer;

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository, KafkaProducer kafkaProducer) {
        this.transactionRepository = transactionRepository;
        this.kafkaProducer = kafkaProducer;
    }

    public TransactionResponseDto create(CreateTransactionRequestDto requestDto) {
        if (requestDto.getAmount() <= 0) {
            throw new IllegalArgumentException("Transaction amount must be positive");
        }
        Transaction transaction = new Transaction();
        transaction.setAccountId(requestDto.getAccountId());
        transaction.setAmount(requestDto.getAmount());
        transaction.setType(requestDto.getType());
        transaction.setStatus(TransactionStatus.INITIATED);
        transactionRepository.save(transaction);

        sendTransactionMessage(transaction, transactionInitiatedTopic);

        return toTransactionResponse(transaction);
    }

    public TransactionResponseDto get(Long id) {
        Transaction transaction = transactionRepository.findById(id).orElse(null);
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction not found");
        }

        return toTransactionResponse(transaction);
    }

    public ListTransactionsResponseDto list(Long accountId) {
        List<Transaction> transactions = transactionRepository.findAllByAccountId(accountId);
        if (transactions == null) {
            throw new IllegalArgumentException("get Transactions error");
        }

        return toListTransactionResponse(transactions);
    }

    public void rollbackUpdateStatus(Long transactionId) {
        updateStatus(transactionId, TransactionStatus.FAILED);
    }

    public void completeUpdateStatus(Long transactionId) {
        updateStatus(transactionId, TransactionStatus.COMPLETED);
    }

    private void updateStatus(Long transactionId, TransactionStatus status) {
        Transaction transaction = transactionRepository.findById(transactionId).orElse(null);
        if (transaction == null) {
            // send .dlt: transaction not found
            throw new IllegalArgumentException("Transaction not found");
        }

        if (transaction.getStatus() != TransactionStatus.INITIATED) {
            return;
        }

        transaction.setStatus(status);
        transactionRepository.save(transaction);
    }

    private void sendTransactionMessage(Transaction transaction, String topic) {
        String messageKey = transaction.getId().toString();
        String messageValue = toMessage(transaction);
        kafkaProducer.sendMessage(topic, messageKey, messageValue);
    }
} 