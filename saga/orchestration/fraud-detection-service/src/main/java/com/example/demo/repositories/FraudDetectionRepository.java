package com.example.demo.repositories;

import com.example.demo.models.FraudDetection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FraudDetectionRepository extends JpaRepository<FraudDetection, Long> { }