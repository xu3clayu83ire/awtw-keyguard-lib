package com.keyguard.interfaces.controller;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

/**
 * REST 全域異常處理器。
 * 集中處理驗證失敗與資源找不到等常見異常，回傳符合 RFC 9457 的 ProblemDetail 格式。
 */
@RestControllerAdvice
public class RestExceptionHandler {

    /**
     * 處理 @Valid 驗證失敗（請求體格式錯誤），回傳 400。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Validation failed");
        problemDetail.setDetail(exception.getBindingResult().getAllErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Request validation failed"));
        return problemDetail;
    }

    /**
     * 處理約束條件違反（如語法層驗證），回傳 400。
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Constraint violation");
        problemDetail.setDetail(exception.getMessage());
        return problemDetail;
    }

    /**
     * 處理資源找不到的情況，回傳 404。
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ProblemDetail handleNotFound(NoSuchElementException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problemDetail.setTitle("Resource not found");
        problemDetail.setDetail(exception.getMessage());
        return problemDetail;
    }
}