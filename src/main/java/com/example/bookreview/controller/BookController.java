package com.example.bookreview.controller;

import com.example.bookreview.dto.BookDTO;
import com.example.bookreview.dto.ReviewDTO;
import com.example.bookreview.exception.NoBooksFoundException; // Import your custom exception
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
    public ResponseEntity<EntityModel<BookDTO>> getBook(@PathVariable Long id) {
        BookDTO book = bookService.getBookById(id);

        if (book == null) {
            throw new NoBooksFoundException("Book with ID " + id + " not found.");
        }

        EntityModel<BookDTO> bookResource = EntityModel.of(book,
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(this.getClass()).getBook(id)).withSelfRel(),
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(this.getClass()).deleteBook(id)).withRel("delete"));

        return ResponseEntity.ok(bookResource);
    }

    // Get All Books (with pagination)
    @GetMapping
    public ResponseEntity<Page<BookDTO>> getAllBooks(
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<BookDTO> books;

        if (year != null) {
            books = bookService.getBooksByYear(year, sortBy, sortDirection, PageRequest.of(page, size));
        } else {
            books = bookService.getAllBooksSorted(sortBy, sortDirection, PageRequest.of(page, size));
        }

        if (books.isEmpty()) {
            throw new NoBooksFoundException("No books found.");
        }

        return ResponseEntity.ok(books);
    }

    // Add Review to Book
    @PostMapping("/{bookId}/review")
    public ResponseEntity<BookDTO> addReviewToBook(@PathVariable Long bookId, @RequestBody ReviewDTO reviewDTO) {
        try {
            BookDTO updatedBook = bookService.addReview(bookId, reviewDTO); // Call service method to add the review
            return new ResponseEntity<>(updatedBook, HttpStatus.CREATED); // Return the updated book with review
        } catch (Exception e) {
            // Handle any errors, for example if book not found
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get Reviews for a Book
    @GetMapping("/{id}/reviews")
    public ResponseEntity<List<ReviewDTO>> getBookReviews(@PathVariable Long id) {
        List<ReviewDTO> reviews = bookService.getReviewsByBookId(id);

        if (reviews.isEmpty()) {
            throw new NoBooksFoundException("No reviews found for the book with ID " + id);
        }

        return ResponseEntity.ok(reviews);
    }

    // Delete Book by ID
    @DeleteMapping("/{id}")
    @CacheEvict(value = "books", key = "#id")  // Clears cache
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        boolean isDeleted = bookService.deleteBook(id);

        if (!isDeleted) {
            throw new NoBooksFoundException("Book with ID " + id + " not found to delete.");
        }

        return ResponseEntity.noContent().build(); // HTTP 204
    }

    // Update Book details by ID
    @PutMapping("/{id}")
    public ResponseEntity<BookDTO> updateBook(@PathVariable Long id, @RequestBody BookDTO bookDTO) {
        // Call service to update the book
        BookDTO updatedBook = bookService.updateBook(id, bookDTO);

        if (updatedBook == null) {
            throw new NoBooksFoundException("Book with ID " + id + " not found to update.");
        }

        return ResponseEntity.ok(updatedBook);
    }

    // Delete Review from Book
    @DeleteMapping("/{bookId}/review/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long bookId, @PathVariable Long reviewId) {
        // Call the service to delete the review
        bookService.deleteReview(bookId, reviewId);
        return ResponseEntity.noContent().build();  // Return 204 No Content
    }
}
