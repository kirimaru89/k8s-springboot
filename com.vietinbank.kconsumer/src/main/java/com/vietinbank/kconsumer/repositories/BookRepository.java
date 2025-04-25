package com.vietinbank.kconsumer.repositories;

import com.vietinbank.kconsumer.models.Book;
import com.vietinbank.kconsumer.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByUser(User user);
}