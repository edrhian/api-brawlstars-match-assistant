package com.edrhian.brawlmatchstats.config;

import com.edrhian.brawlmatchstats.brawlstars.BrawlStarsApiException;
import com.edrhian.brawlmatchstats.player.PlayerAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

/**
 * Turns the exceptions of our services into clear HTTP errors.
 * The body is a standard "problem detail": {"status": 404, "detail": "..."}
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    // Player not registered
    @ExceptionHandler(NoSuchElementException.class)
    public ProblemDetail handleNotFound(NoSuchElementException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(PlayerAlreadyExistsException.class)
    public ProblemDetail handleAlreadyExists(PlayerAlreadyExistsException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    // For example an empty tag
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleBadRequest(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    // 400 and 404 of the BrawlStars API are the caller's fault, anything else (bad key, limit, maintenance) is 502
    @ExceptionHandler(BrawlStarsApiException.class)
    public ProblemDetail handleBrawlStarsError(BrawlStarsApiException e) {
        HttpStatus status = switch (e.getStatusCode()) {
            case 400 -> HttpStatus.BAD_REQUEST;
            case 404 -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.BAD_GATEWAY;
        };
        return ProblemDetail.forStatusAndDetail(status, e.getMessage());
    }
}