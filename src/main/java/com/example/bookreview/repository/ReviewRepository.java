package com.example.bookreview.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.bookreview.models.Review;

import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    Optional<Review> findById(UUID reviewId);
}
