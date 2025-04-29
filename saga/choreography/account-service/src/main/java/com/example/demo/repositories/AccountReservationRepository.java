package com.example.demo.repositories;

import com.example.demo.models.AccountReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountReservationRepository extends JpaRepository<AccountReservation, Long> {
    Optional<AccountReservation> findByTransactionId(Long transactionId);
}
