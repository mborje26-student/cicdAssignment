package com.example.bookreview.controller;

import com.example.bookreview.dto.BookDTO;
import com.example.bookreview.dto.ReviewDTO;
import com.example.bookreview.exception.NoBooksFoundException;
import com.example.bookreview.service.BookService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    // Add Book
    @PostMapping
    public ResponseEntity<BookDTO> addBook(@RequestBody BookDTO bookDTO) {
        BookDTO savedBook = bookService.addBook(bookDTO);
        return ResponseEntity.ok(savedBook);
    }

    // Get a Single Book by ID
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<BookDTO>> getBook(@PathVariable UUID id) {
        BookDTO book = bookService.getBookById(id);

        EntityModel<BookDTO> bookResource = EntityModel.of(book,
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(this.getClass()).getBook(id)).withSelfRel(),
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(this.getClass()).deleteBook(id)).withRel("delete"));

        return ResponseEntity.ok(bookResource);
    }

    // Get All Books (with optional year filter + sorting + pagination)
    @GetMapping
    public ResponseEntity<Page<BookDTO>> getAllBooks(
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<BookDTO> books = (year != null)
                ? bookService.getBooksByYear(year, sortBy, sortDirection, PageRequest.of(page, size))
                : bookService.getAllBooksSorted(sortBy, sortDirection, PageRequest.of(page, size));

        if (books.isEmpty()) {
            throw new NoBooksFoundException("No books found.");
        }

        return ResponseEntity.ok(books);
    }

    // Add Review to Book
    @PostMapping("/{bookId}/review")
    public ResponseEntity<BookDTO> addReviewToBook(@PathVariable UUID bookId, @RequestBody ReviewDTO reviewDTO) {
        BookDTO updatedBook = bookService.addReview(bookId, reviewDTO);
        return new ResponseEntity<>(updatedBook, HttpStatus.CREATED);
    }

    // Get Reviews for a Book
    @GetMapping("/{id}/reviews")
    public ResponseEntity<List<ReviewDTO>> getBookReviews(@PathVariable UUID id) {
        List<ReviewDTO> reviews = bookService.getReviewsByBookId(id);

        if (reviews.isEmpty()) {
            throw new NoBooksFoundException("No reviews found for the book with ID " + id);
        }

        return ResponseEntity.ok(reviews);
    }

    // Delete Book by ID
    @DeleteMapping("/{id}")
    @CacheEvict(value = "books", key = "#id")
    public ResponseEntity<Void> deleteBook(@PathVariable UUID id) {
        boolean isDeleted = bookService.deleteBook(id);

        if (!isDeleted) {
            throw new NoBooksFoundException("Book with ID " + id + " not found to delete.");
        }

        return ResponseEntity.noContent().build();
    }

    // Update Book details by ID
    @PutMapping("/{id}")
    public ResponseEntity<BookDTO> updateBook(@PathVariable UUID id, @RequestBody BookDTO bookDTO) {
        BookDTO updatedBook = bookService.updateBook(id, bookDTO);

        if (updatedBook == null) {
            throw new NoBooksFoundException("Book with ID " + id + " not found to update.");
        }

        return ResponseEntity.ok(updatedBook);
    }

    // Delete Review from Book
    @DeleteMapping("/{bookId}/review/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable UUID bookId, @PathVariable UUID reviewId) {
        bookService.deleteReview(bookId, reviewId);
        return ResponseEntity.noContent().build();
    }
}
