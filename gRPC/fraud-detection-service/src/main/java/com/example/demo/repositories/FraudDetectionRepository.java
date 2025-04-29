package com.example.demo.repositories;

import com.example.demo.models.FraudDetection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FraudDetectionRepository extends JpaRepository<FraudDetection, Long> {
    Boolean existsByTransactionId(Long transactionId);
    Optional<FraudDetection> findByTransactionId(Long transactionId);
}