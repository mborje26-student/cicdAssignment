package com.example.bookreview;

import com.example.bookreview.dto.BookDTO;
import com.example.bookreview.models.Book;
import com.example.bookreview.repository.BookRepository;
import com.example.bookreview.repository.ReviewRepository;
import com.example.bookreview.service.BookService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BookReviewApplicationTest {

    private BookRepository bookRepository;
    private ReviewRepository reviewRepository;
    private BookService bookService;

    @BeforeEach
    void setUp() {
        bookRepository = mock(BookRepository.class);
        reviewRepository = mock(ReviewRepository.class);
        bookService = new BookService(bookRepository, reviewRepository);
    }

    @Test
    void testAddBook() {
        LocalDate publishedDate = LocalDate.of(2008, 8, 1);
        BookDTO bookDTO = new BookDTO(null, "Clean Code", "Robert C. Martin", publishedDate);

        Book savedBook = new Book(bookDTO.getTitle(), bookDTO.getAuthor(), publishedDate);

        Long mockId = 1L;
        savedBook.setId(mockId);

        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);
        BookDTO result = bookService.addBook(bookDTO);
        System.out.println("Returned BookDTO ID: " + result.getId());

        assertNotNull(result.getId(), "BookDTO ID should not be null after saving.");
        assertEquals("Clean Code", result.getTitle());
        assertEquals("Robert C. Martin", result.getAuthor());
        assertEquals(publishedDate, result.getPublishedDate());
    }



    @Test
    void testGetBookById() {
        LocalDate publishedDate = LocalDate.of(2008, 8, 1);
        UUID bookId = UUID.randomUUID();

    }
}