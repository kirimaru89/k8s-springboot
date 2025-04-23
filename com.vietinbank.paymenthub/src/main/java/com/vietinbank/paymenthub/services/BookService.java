package com.vietinbank.paymenthub.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.vietinbank.paymenthub.models.Book;
import com.vietinbank.paymenthub.models.User;
import com.vietinbank.paymenthub.repositories.BookRepository;
import com.vietinbank.paymenthub.repositories.UserRepository;
import com.vietinbank.paymenthub.security.JwtUtil;

import io.opentelemetry.instrumentation.annotations.WithSpan;

@Service
public class BookService {
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    public List<Book> getUserBooks(String token) {
        String username = jwtUtil.validateToken(token);
        User user = userRepository.findByUsername(username).orElseThrow();
        return bookRepository.findByUser(user);
    }

    public Book addBook(String token, Book book) {
        String username = jwtUtil.validateToken(token);
        User user = userRepository.findByUsername(username).orElseThrow();
        book.setUser(user);
        return bookRepository.save(book);
    }

    @WithSpan
    @Cacheable(value = "books", key = "#id")
    public Optional<Book> getBookById(Long id) {
        System.out.println("Fetching book from database...");
        return bookRepository.findById(id);
    }
}
