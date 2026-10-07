package com.edrhian.brawlmatchstats.brawlstars;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the client against a small fake server running on your computer.
 * No internet and no real API key are needed.
 */
class BrawlStarsClientTest {

    private static final String PLAYER_JSON = """
            {
              "tag": "#2VU8PGCC",
              "name": "edrhian27",
              "nameColor": "0xff1ba5f5",
              "icon": { "id": 28000006 },
              "trophies": 111557,
              "rankedRankName": "LEGENDARY I",
              "club": { "tag": "#292CQ0G2P", "name": "WACHINES EMPIRE" },
              "brawlers": []
            }
            """;

    private static final String BATTLE_LOG_JSON = """
            {
              "items": [
                {
                  "battleTime": "20261007T070930.000Z",
                  "event": { "id": 15000018, "mode": "heist", "modeId": 2, "map": "Kaboom Canyon" },
                  "battle": {
                    "mode": "heist",
                    "type": "soloRanked",
                    "result": "victory",
                    "duration": 85,
                    "starPlayer": { "tag": "#9J8PRJRQL", "name": "player", "brawler": { "id": 16000003, "name": "BROCK" } },
                    "teams": [
                      [
                        { "tag": "#P0R9Y9J09", "name": "watata", "brawler": { "id": 16000001, "name": "COLT", "power": 11 } },
                        { "tag": "#2VU8PGCC", "name": "edrhian27", "brawler": { "id": 16000018, "name": "DARRYL", "power": 11 } },
                        { "tag": "#P2J88GQG", "name": "guilie", "brawler": { "id": 16000040, "name": "AMBER", "power": 11 } }
                      ],
                      [
                        { "tag": "#9J8PRJRQL", "name": "enemy1", "brawler": { "id": 16000003, "name": "BROCK", "power": 11 } },
                        { "tag": "#2R0P8LPR9", "name": "enemy2", "brawler": { "id": 16000025, "name": "CARL", "power": 11 } },
                        { "tag": "#Y8Q8UPCGV", "name": "enemy3", "brawler": { "id": 16000042, "name": "BYRON", "power": 11 } }
                      ]
                    ]
                  }
                }
              ],
              "paging": { "cursors": {} }
            }
            """;

    private HttpServer server;
    private String baseUrl;

    // What the fake server saw and what it answers
    private final AtomicReference<String> lastPath = new AtomicReference<>();
    private final AtomicReference<String> lastAuthorization = new AtomicReference<>();
    private volatile int responseStatus = 200;
    private volatile String responseBody = "{}";

    @BeforeEach
    void startFakeServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", exchange -> {
            lastPath.set(exchange.getRequestURI().getRawPath());
            lastAuthorization.set(exchange.getRequestHeaders().getFirst("Authorization"));

            byte[] body = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(responseStatus, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        });
        server.start();
        baseUrl = "http://localhost:" + server.getAddress().getPort() + "/v1";
    }

    @AfterEach
    void stopFakeServer() {
        server.stop(0);
    }

    @Test
    void getsPlayerAndIgnoresUnknownFields() {
        responseBody = PLAYER_JSON;
        BrawlStarsClient client = new BrawlStarsClient(baseUrl, "test-key");

        BrawlStarsPlayerResponse player = client.getPlayer("#2VU8PGCC");

        assertThat(player.tag()).isEqualTo("#2VU8PGCC");
        assertThat(player.name()).isEqualTo("edrhian27");
        assertThat(player.trophies()).isEqualTo(111557);
        assertThat(player.rankedRankName()).isEqualTo("LEGENDARY I");
    }

    @Test
    void sendsTagEncodedAndApiKeyAsBearerToken() {
        responseBody = PLAYER_JSON;
        BrawlStarsClient client = new BrawlStarsClient(baseUrl, "test-key");

        client.getPlayer("#2VU8PGCC");

        assertThat(lastPath.get()).isEqualTo("/v1/players/%232VU8PGCC");
        assertThat(lastAuthorization.get()).isEqualTo("Bearer test-key");
    }

    @Test
    void addsHashAndUppercasesTagWhenMissing() {
        responseBody = PLAYER_JSON;
        BrawlStarsClient client = new BrawlStarsClient(baseUrl, "test-key");

        client.getPlayer("2vu8pgcc");

        assertThat(lastPath.get()).isEqualTo("/v1/players/%232VU8PGCC");
    }

    @Test
    void getsBattleLog() {
        responseBody = BATTLE_LOG_JSON;
        BrawlStarsClient client = new BrawlStarsClient(baseUrl, "test-key");

        List<BrawlStarsBattleLogResponse.Item> battles = client.getBattleLog("#2VU8PGCC");

        assertThat(lastPath.get()).isEqualTo("/v1/players/%232VU8PGCC/battlelog");
        assertThat(battles).hasSize(1);

        BrawlStarsBattleLogResponse.Item battle = battles.get(0);
        assertThat(battle.battleTime()).isEqualTo("20261007T070930.000Z");
        assertThat(battle.event().map()).isEqualTo("Kaboom Canyon");
        assertThat(battle.battle().mode()).isEqualTo("heist");
        assertThat(battle.battle().type()).isEqualTo("soloRanked");
        assertThat(battle.battle().result()).isEqualTo("victory");

        assertThat(battle.battle().teams()).hasSize(2);
        assertThat(battle.battle().teams().get(0)).hasSize(3);
        assertThat(battle.battle().teams().get(0).get(1).tag()).isEqualTo("#2VU8PGCC");
        assertThat(battle.battle().teams().get(0).get(1).brawler().name()).isEqualTo("DARRYL");
        assertThat(battle.battle().teams().get(1).get(2).brawler().name()).isEqualTo("BYRON");
    }

    @Test
    void throwsApiExceptionWhenPlayerIsNotFound() {
        responseStatus = 404;
        responseBody = "{\"reason\":\"notFound\"}";
        BrawlStarsClient client = new BrawlStarsClient(baseUrl, "test-key");

        assertThatThrownBy(() -> client.getPlayer("#NOPLAYER"))
                .isInstanceOf(BrawlStarsApiException.class)
                .satisfies(error -> assertThat(((BrawlStarsApiException) error).getStatusCode()).isEqualTo(404));
    }

    @Test
    void throwsClearErrorWhenApiKeyIsMissing() {
        BrawlStarsClient client = new BrawlStarsClient(baseUrl, "");

        assertThatThrownBy(() -> client.getPlayer("#2VU8PGCC"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("BRAWLSTARS_API_KEY");
    }
}