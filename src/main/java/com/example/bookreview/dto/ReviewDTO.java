package com.example.bookreview.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public class ReviewDTO {
    private int rating;  // Rating of the book, this can be updated
    private String comment;  // Comment for the review, this can be updated
    private String reviewerName;  // Name of the person who wrote the review
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate reviewDate;  // The date when the review was created

    // Constructor for creating a new review (full constructor)
    public ReviewDTO(Long id, int rating, String comment, String reviewerName, LocalDate reviewDate) {

        this.rating = rating;
        this.comment = comment;
        this.reviewerName = reviewerName;
        this.reviewDate = reviewDate;
    }

    // Default constructor (useful for deserialization if needed)
    public ReviewDTO() {}

    // Getters and Setters

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public LocalDate getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(LocalDate reviewDate) {
        this.reviewDate = reviewDate;
    }
}
