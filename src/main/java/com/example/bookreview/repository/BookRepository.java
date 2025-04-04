package com.example.bookreview.repository;

import com.example.bookreview.models.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    boolean existsByTitleAndAuthor(String title, String author);
    @EntityGraph(attributePaths = "reviews")
    Optional<Book> findById(Long id);

    Page<Book> findAllByOrderByPublishedDateAsc(Pageable pageable);

    Page<Book> findAllByOrderByPublishedDateDesc(Pageable pageable);
    Page<Book> findByPublishedDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

}
