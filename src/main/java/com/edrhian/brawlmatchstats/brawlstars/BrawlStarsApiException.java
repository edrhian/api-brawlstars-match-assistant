package com.edrhian.brawlmatchstats.brawlstars;

/**
 * Thrown when the BrawlStars API answers with an error (403, 404, 429, 503...).
 */
public class BrawlStarsApiException extends RuntimeException {

    private final int statusCode;

    public BrawlStarsApiException(int statusCode, String message) {
        super("BrawlStars API error " + statusCode + ": " + message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}