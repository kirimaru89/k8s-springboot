package com.vietinbank.kproducer.repositories;

import java.util.Optional;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.vietinbank.kproducer.models.Book;
import com.vietinbank.kproducer.models.User;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {
    Page<Book> findByUser(User user, Pageable pageable);

    Optional<Book> findByIdAndUser(Long id, User user);

    boolean existsByIdAndUser(Long id, User user);
}
