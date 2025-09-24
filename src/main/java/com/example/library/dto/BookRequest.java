package com.example.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record BookRequest (  @NotBlank String title,
                             String isbn,
                             @PastOrPresent LocalDate publishedDate,
                             @NotNull Long authorId
) {}
