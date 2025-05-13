package com.vietinbank.kproducer.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.vietinbank.kproducer.models.Book;
import com.vietinbank.kproducer.models.User;
import com.vietinbank.kproducer.repositories.BookRepository;
import com.vietinbank.kproducer.repositories.UserRepository;
import com.vietinbank.kproducer.dto.request.book.BookCreationDto;
import com.vietinbank.kproducer.dto.request.book.BookFilterDto;
import com.vietinbank.kproducer.dto.response.book.BookResponseDto;

@Service
public class BookService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return null;
    }

    public ResponseEntity<Map<String, Object>> list(int page, int size, BookFilterDto filterDTO) {
        Specification<Book> specification = buildSpecification(filterDTO);
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> books = bookRepository.findAll(specification, pageable);

        List<BookResponseDto> booksResponse = books.stream()
            .map(book -> BookResponseDto.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .build())
            .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("books", booksResponse);
        response.put("page", page);
        response.put("size", size);
        response.put("totalElements", books.getTotalElements());
        response.put("totalPages", books.getTotalPages());
        response.put("numberOfElements", books.getNumberOfElements());

        return ResponseEntity.ok(response);
    }

    private Specification<Book> buildSpecification(BookFilterDto filterDTO) {
        Specification<Book> spec = Specification.where(null);

        if(filterDTO.getTitle() != null && !filterDTO.getTitle().isEmpty()) {
            spec = spec.and((root, criteriaQuery, criteriaBuilder) ->
                    criteriaBuilder.like(root.get("title"), "%" + filterDTO.getTitle() + "%"));
        }
        if(filterDTO.getAuthor() != null && !filterDTO.getAuthor().isEmpty()) {
            spec = spec.and((root, criteriaQuery, criteriaBuilder) ->
                    criteriaBuilder.like(root.get("author"), "%" + filterDTO.getAuthor() + "%"));
        }
        if(filterDTO.getUserId() != null && !filterDTO.getUserId().isEmpty()) {
            spec = spec.and((root, criteriaQuery, criteriaBuilder) ->
                    root.get("user").get("id").in(filterDTO.getUserId()));
        }

        return spec;
    }

    public BookResponseDto create(BookCreationDto bookRequest) {
        User user = userRepository.findById(getCurrentUserId()).orElseThrow();

        Book book = Book.builder()
            .title(bookRequest.getTitle())
            .author(bookRequest.getAuthor())
            .user(user)
            .build();

        book = bookRepository.save(book);

        BookResponseDto response = BookResponseDto.builder()
            .id(book.getId())
            .title(book.getTitle())
            .author(book.getAuthor())
            .build();

        return response;
    }

    @Cacheable(value = "books", key = "#id")
    public BookResponseDto get(Long id) {
        Book book = bookRepository.findById(id).orElse(null);

        if (book == null) {
            return null;
        }

        BookResponseDto response = BookResponseDto.builder()
            .id(book.getId())
            .title(book.getTitle())
            .author(book.getAuthor())
            .build();

        return response;
    }

    @CachePut(value = "books", key = "#id")
    public BookResponseDto update(BookCreationDto bookRequest, Long id) {
        User user = userRepository.findById(getCurrentUserId()).orElseThrow();
        if (!bookRepository.existsById(id)) {
            return null;
        }

        Book book = Book.builder()
            .id(id)
            .title(bookRequest.getTitle())
            .author(bookRequest.getAuthor())
            .user(user)
            .build();

        book = bookRepository.save(book);

        BookResponseDto response = BookResponseDto.builder()
            .id(book.getId())
            .title(book.getTitle())
            .author(book.getAuthor())
            .build();

        return response;
    }

    @CacheEvict(value = "books", key = "#id")
    public void delete(Long id) {
        if (!bookRepository.existsById(id)) {
            return;
        }

        bookRepository.deleteById(id);
    }

    private long getCurrentUserId() {
        return 2;
    }
}
