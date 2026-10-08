package com.edrhian.brawlmatchstats.player;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints to manage players. The tag goes in the URL without "#" (or with %23).
 */
@RestController
@RequestMapping("/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PostMapping("/{tag}")
    @ResponseStatus(HttpStatus.CREATED)
    public PlayerDto addPlayer(@PathVariable String tag,
                               @Valid @RequestBody(required = false) RegisterPlayerRequest request) {
        // No body means all defaults
        RegisterPlayerRequest body = request != null ? request : new RegisterPlayerRequest(null, null);
        return playerService.registerPlayer(tag, body.trackingEnabled(), body.trackingTypes());
    }

    @GetMapping
    public List<PlayerDto> getAllPlayers() {
        return playerService.getAllPlayers();
    }

    @GetMapping("/{tag}")
    public PlayerDto getPlayer(@PathVariable String tag) {
        return playerService.getPlayer(tag);
    }

    @PostMapping("/{tag}/tracking/enable")
    public PlayerDto enableTracking(@PathVariable String tag) {
        return playerService.setTrackingEnabled(tag, true);
    }

    @PostMapping("/{tag}/tracking/disable")
    public PlayerDto disableTracking(@PathVariable String tag) {
        return playerService.setTrackingEnabled(tag, false);
    }

    @PutMapping("/{tag}/tracking/types")
    public PlayerDto changeTrackingTypes(@PathVariable String tag,
                                         @Valid @RequestBody TrackingTypesRequest request) {
        return playerService.setTrackingTypes(tag, request.trackingTypes());
    }
}