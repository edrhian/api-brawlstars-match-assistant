package com.edrhian.brawlmatchstats.brawlstars;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

/**
 * Makes the calls to the official BrawlStars API. It only gets data, it does not save anything.
 */
@Component
public class BrawlStarsClient {

    private final RestClient restClient;
    private final String baseUrl;
    private final String apiKey;

    public BrawlStarsClient(
            @Value("${brawlstars.api.base-url:https://api.brawlstars.com/v1}") String baseUrl,
            @Value("${brawlstars.api.key:}") String apiKey) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;

        // Timeouts, so a slow API cannot block the scheduled job forever
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    // GET /players/{tag}
    public BrawlStarsPlayerResponse getPlayer(String tag) {
        return get(buildUri(tag, ""), BrawlStarsPlayerResponse.class);
    }

    // GET /players/{tag}/battlelog (the API returns the last 25 battles)
    public List<BrawlStarsBattleLogResponse.Item> getBattleLog(String tag) {
        BrawlStarsBattleLogResponse response = get(buildUri(tag, "/battlelog"), BrawlStarsBattleLogResponse.class);
        if (response == null || response.items() == null) {
            return List.of();
        }
        return response.items();
    }

    private <T> T get(URI uri, Class<T> responseType) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "BrawlStars API key is missing. Set the BRAWLSTARS_API_KEY environment variable.");
        }

        return restClient.get()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    int status = response.getStatusCode().value();
                    throw new BrawlStarsApiException(status, describe(status));
                })
                .body(responseType);
    }

    // The "#" of the tag must be sent as %23, so we build the URI ourselves
    private URI buildUri(String tag, String suffix) {
        if (tag == null || tag.isBlank()) {
            throw new IllegalArgumentException("The player tag cannot be empty");
        }
        String cleanTag = tag.trim().toUpperCase();
        if (!cleanTag.startsWith("#")) {
            cleanTag = "#" + cleanTag;
        }
        String encodedTag = URLEncoder.encode(cleanTag, StandardCharsets.UTF_8);
        return URI.create(baseUrl + "/players/" + encodedTag + suffix);
    }

    private String describe(int status) {
        return switch (status) {
            case 400 -> "Invalid request (check the player tag)";
            case 403 -> "The API key is invalid or this IP address is not allowed for the key";
            case 404 -> "Player not found";
            case 429 -> "Too many requests, the API limit was reached";
            case 503 -> "The BrawlStars API is in maintenance";
            default -> "Unexpected error";
        };
    }
}