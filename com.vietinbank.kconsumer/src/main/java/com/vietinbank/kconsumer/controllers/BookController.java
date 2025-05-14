package com.vietinbank.kconsumer.controllers;

import com.vietinbank.kconsumer.services.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import com.vietinbank.kconsumer.dto.request.book.BookCreationDto;
import com.vietinbank.kconsumer.dto.request.book.BookFilterDto;
import com.vietinbank.kconsumer.dto.response.book.BookResponseDto;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@RestController
@RequestMapping("/api/books")
public class BookController {
    @Autowired
    private BookService bookService;

    @Operation(summary = "Get Book List", description = "Get Book List API")
    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @ModelAttribute BookFilterDto filterDTO
    ) {
        return bookService.list(page, size, filterDTO);
    }

    @Operation(summary = "Add Book", description = "Add Book API")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<BookResponseDto> create(@Valid @RequestBody BookCreationDto bookRequest) {
        BookResponseDto response = bookService.create(bookRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get the Book", description = "Get the Book API")
    @GetMapping("/{id}")
    public ResponseEntity<BookResponseDto> get(
            @Parameter(description = "ID of the book to be fetched") @PathVariable Long id) {
        return ResponseEntity.ok(bookService.get(id));
    }

    @Operation(summary = "Update Book", description = "Update Book API")
    @PutMapping("/{id}")
    public ResponseEntity<BookResponseDto> update(
            @Valid @RequestBody BookCreationDto bookRequest,
            @Parameter(description = "ID of the book to be updated") @PathVariable Long id) {
        return ResponseEntity.ok(bookService.update(bookRequest, id));
    }

    @Operation(summary = "Remove Book", description = "Remove Book API")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@Parameter(description = "ID of the book to be removed") @PathVariable Long id) {
        bookService.delete(id);
    }
}
