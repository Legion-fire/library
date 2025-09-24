package com.example.library.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class RestExceptionHandler {

    record ApiError(int status, String error, String message, String path, Instant timestamp, Map<String, String> validation) {}

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(NotFoundException ex, HttpServletRequest req) {
        return new ApiError(404, "Not Found", ex.getMessage(), req.getRequestURI(), Instant.now(), null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handle(NoResourceFoundException ex, HttpServletRequest req) {
        return new ApiError(404, "Not Found", ex.getMessage(), req.getRequestURI(), Instant.now(), null);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.put(fe.getField(), fe.getDefaultMessage());
        }
        return new ApiError(400, "Bad Request", "Validation failed", req.getRequestURI(), Instant.now(), errors);
    }

    @ExceptionHandler({
            ConstraintViolationException.class,
            HttpMessageNotReadableException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequest(Exception ex, HttpServletRequest req) {
        return new ApiError(400, "Bad Request", ex.getMessage(), req.getRequestURI(), Instant.now(), null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflict(DataIntegrityViolationException ex, HttpServletRequest req) {
        return new ApiError(409, "Conflict", "Data integrity violation", req.getRequestURI(), Instant.now(), null);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleOther(Exception ex, HttpServletRequest req) {
        return new ApiError(500, "Internal Server Error", ex.getMessage(), req.getRequestURI(), Instant.now(), null);
    }
}