package com.example.bookreview.service;

import com.example.bookreview.dto.BookDTO;
import com.example.bookreview.dto.ReviewDTO;
import com.example.bookreview.exception.BookAlreadyExistsException;
import com.example.bookreview.exception.NoBooksFoundException;
import com.example.bookreview.models.Book;
import com.example.bookreview.models.Review;
import com.example.bookreview.repository.BookRepository;
import com.example.bookreview.repository.ReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;

    public BookService(BookRepository bookRepository, ReviewRepository reviewRepository) {
        this.bookRepository = bookRepository;
        this.reviewRepository = reviewRepository;
    }

    public BookDTO addBook(BookDTO bookDTO) {
        if (bookRepository.existsByTitleAndAuthor(bookDTO.getTitle(), bookDTO.getAuthor())) {
            throw new BookAlreadyExistsException("Book with title '" + bookDTO.getTitle() + "' by author '" + bookDTO.getAuthor() + "' already exists.");
        }

        Book book = new Book();
        book.setTitle(bookDTO.getTitle());
        book.setAuthor(bookDTO.getAuthor());
        book.setPublishedDate(bookDTO.getPublishedDate());

        Book savedBook = bookRepository.save(book);
        return convertToDTO(savedBook);
    }

    public BookDTO getBookById(Long id) {
        // Call the updated findById method that uses @EntityGraph
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new NoBooksFoundException("Book not found with ID: " + id));

        return convertToDTO(book); // Return the book as DTO
    }

    public BookDTO addReview(Long bookId, ReviewDTO reviewDTO) {
        // Retrieve the book
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new NoBooksFoundException("Book not found"));

        // Create and set the new review
        Review review = new Review();
        review.setReviewerName(reviewDTO.getReviewerName());
        review.setComment(reviewDTO.getComment());
        review.setRating(reviewDTO.getRating());
        review.setReviewDate(reviewDTO.getReviewDate());
        review.setBook(book); // Associate review with the book

        // Initialize reviews list if it's null
        if (book.getReviews() == null) {
            book.setReviews(new ArrayList<>());
        }

        // Add the review to the book's reviews list
        book.getReviews().add(review);

        // Save the review and the book
        reviewRepository.save(review);
        bookRepository.save(book);

        // Return the updated book as DTO
        return convertToDTO(book);
    }

    public boolean deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new NoBooksFoundException("Book not found with ID: " + id));
        bookRepository.delete(book);
        return true; // Return true after deletion to indicate success
    }

    private ReviewDTO convertReviewToDTO(Review review) {
        return new ReviewDTO(
                review.getId(),
                review.getRating(),
                review.getComment(),
                review.getReviewerName(),
                review.getReviewDate()
        );
    }

    // Correct method to convert Book entity to BookDTO
    private BookDTO convertToDTO(Book book) {
        return new BookDTO(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getPublishedDate(),
                book.getReviews() != null
                        ? book.getReviews().stream().map(this::convertReviewToDTO).collect(Collectors.toList())
                        : new ArrayList<>()
        );
    }

    // Get all books with pagination
    public Page<BookDTO> getAllBooks(Pageable pageable) {
        Page<Book> bookPage = bookRepository.findAll(pageable);
        return bookPage.map(this::convertToDTO);
    }

    // Get all books sorted by a given field with pagination
    public Page<BookDTO> getAllBooksSorted(String sortBy, String sortDirection, Pageable pageable) {
        Sort sort = sortDirection.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        Page<Book> booksPage = bookRepository.findAll(sortedPageable);
        return booksPage.map(this::convertToDTO);  // Convert Page<Book> to Page<BookDTO>
    }

    // Get books filtered by year with pagination
    public Page<BookDTO> getBooksByYear(int year, String sortBy, String sortDirection, Pageable pageable) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        // Creating Sort based on the given sort direction
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        // Create Pageable with page size, page number and sorting
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        // Fetch the paginated and sorted results
        Page<Book> bookPage = bookRepository.findByPublishedDateBetween(startDate, endDate, sortedPageable);

        // Return mapped results as BookDTO
        return bookPage.map(this::convertToDTO);
    }

    public List<ReviewDTO> getReviewsByBookId(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new NoBooksFoundException("Book not found"));

        return book.getReviews().stream().map(this::convertReviewToDTO).collect(Collectors.toList());
    }

    public BookDTO updateBook(Long id, BookDTO bookDTO) {
        // Check if the book exists in the database
        Optional<Book> bookOptional = bookRepository.findById(id);

        if (bookOptional.isPresent()) {
            Book book = bookOptional.get();

            // Update book fields
            book.setTitle(bookDTO.getTitle());
            book.setAuthor(bookDTO.getAuthor());
            book.setPublishedDate(bookDTO.getPublishedDate());

            // Save the updated book
            Book updatedBook = bookRepository.save(book);

            // Convert to DTO and return
            return convertToDTO(updatedBook);
        } else {
            return null;  // Book not found
        }
    }

    public void deleteReview(Long bookId, Long reviewId) {
        // Find the book by its ID
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new NoBooksFoundException("Book not found"));

        // Find the review by its ID
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NoBooksFoundException("Review not found"));

        // Remove the review from the book's reviews list
        book.getReviews().remove(review);

        // Save the updated book object
        bookRepository.save(book);

        // Delete the review from the review repository
        reviewRepository.delete(review);
    }

    public Book getBookById(UUID bookId) {
        return null;
    }
}
