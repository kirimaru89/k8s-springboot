// package com.vietinbank.paymenthub.controllers;

// import java.util.concurrent.CompletableFuture;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;
// import io.micrometer.core.annotation.Timed;

// import com.vietinbank.paymenthub.services.ReactiveApp1Service;
// import com.vietinbank.paymenthub.services.BookService;

// import com.vietinbank.paymenthub.models.Book;
// import java.util.Optional;
// import org.springframework.web.bind.annotation.PathVariable;

// @RestController
// @RequestMapping("/api/test")
// public class TestController {
//     @Autowired
//     private BookService bookService;

//     // @Autowired
//     // private LoggingService loggingService;

//     @Autowired
//     private ReactiveApp1Service reactiveApp1Service;

//     @GetMapping("/fire-forget")
//     public String fireAndForget() {
//         // Start the async task but don't wait for it
//         // asyncApp1Service.fireAndForget();
//         reactiveApp1Service.fireAndForgetReactive();
//         // Return immediately
//         return "Request initiated, not waiting for result";
//     }

//     @GetMapping("/get-book/{id}")
//     public Optional<Book> getBookById(@PathVariable Long id) {
//         return bookService.getBookById(id);
//     }
// }