package com.example.library.dto;

import java.time.LocalDate;

public record BookResponse(Long id,
                           String title,
                           String isbn,
                           LocalDate publishedDate,
                           AuthorResponse author) {
}
