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
import java.util.*;
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
            throw new BookAlreadyExistsException("Book with title '" + bookDTO.getTitle()
                    + "' by author '" + bookDTO.getAuthor() + "' already exists.");
        }

        Book book = new Book();
        book.setTitle(bookDTO.getTitle());
        book.setAuthor(bookDTO.getAuthor());
        book.setPublishedDate(bookDTO.getPublishedDate());

        Book savedBook = bookRepository.save(book);
        return convertToDTO(savedBook);
    }

    public BookDTO getBookById(UUID id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new NoBooksFoundException("Book not found with ID: " + id));
        return convertToDTO(book);
    }

    public BookDTO addReview(UUID bookId, ReviewDTO reviewDTO) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new NoBooksFoundException("Book not found with ID: " + bookId));

        Review review = new Review();
        review.setReviewerName(reviewDTO.getReviewerName());
        review.setComment(reviewDTO.getComment());
        review.setRating(reviewDTO.getRating());
        review.setReviewDate(reviewDTO.getReviewDate());
        review.setBook(book);

        if (book.getReviews() == null) {
            book.setReviews(new ArrayList<>());
        }

        book.getReviews().add(review);

        reviewRepository.save(review);
        bookRepository.save(book);

        return convertToDTO(book);
    }

    public boolean deleteBook(UUID id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new NoBooksFoundException("Book not found with ID: " + id));
        bookRepository.delete(book);
        return true;
    }

    public List<ReviewDTO> getReviewsByBookId(UUID bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new NoBooksFoundException("Book not found with ID: " + bookId));

        return book.getReviews() != null
                ? book.getReviews().stream().map(this::convertReviewToDTO).collect(Collectors.toList())
                : Collections.emptyList();
    }

    public BookDTO updateBook(UUID id, BookDTO bookDTO) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new NoBooksFoundException("Book not found with ID: " + id));

        book.setTitle(bookDTO.getTitle());
        book.setAuthor(bookDTO.getAuthor());
        book.setPublishedDate(bookDTO.getPublishedDate());

        Book updatedBook = bookRepository.save(book);
        return convertToDTO(updatedBook);
    }

    public void deleteReview(UUID bookId, UUID reviewId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new NoBooksFoundException("Book not found with ID: " + bookId));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NoBooksFoundException("Review not found with ID: " + reviewId));

        if (book.getReviews() != null && book.getReviews().contains(review)) {
            book.getReviews().remove(review);
            bookRepository.save(book);
        }

        reviewRepository.delete(review);
    }

    public Page<BookDTO> getAllBooks(Pageable pageable) {
        Page<Book> bookPage = bookRepository.findAll(pageable);
        return bookPage.map(this::convertToDTO);
    }

    public Page<BookDTO> getAllBooksSorted(String sortBy, String sortDirection, Pageable pageable) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        return bookRepository.findAll(sortedPageable).map(this::convertToDTO);
    }

    public Page<BookDTO> getBooksByYear(int year, String sortBy, String sortDirection, Pageable pageable) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        return bookRepository.findByPublishedDateBetween(startDate, endDate, sortedPageable).map(this::convertToDTO);
    }

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

    private ReviewDTO convertReviewToDTO(Review review) {
        return new ReviewDTO(
                review.getId(),
                review.getRating(),
                review.getComment(),
                review.getReviewerName(),
                review.getReviewDate()
        );
    }
}
