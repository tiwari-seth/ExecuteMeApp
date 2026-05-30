package com.executeme.broker.angelone.web;

import com.executeme.broker.session.domain.BrokerSession;
import com.executeme.broker.session.service.BrokerSessionService;
import com.executeme.auth.service.OAuthStateService;
import com.executeme.users.domain.TelegramUser;
import com.executeme.users.service.TelegramUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Broker OAuth")
public class AngelCallbackController {

    private final OAuthStateService oauthStateService;
    private final BrokerSessionService brokerSessionService;
    private final TelegramUserService telegramUserService;

    public AngelCallbackController(OAuthStateService oauthStateService,
                                   BrokerSessionService brokerSessionService,
                                   TelegramUserService telegramUserService) {
        this.oauthStateService = oauthStateService;
        this.brokerSessionService = brokerSessionService;
        this.telegramUserService = telegramUserService;
    }

    @GetMapping(value = "/broker/angel/callback", produces = MediaType.TEXT_HTML_VALUE)
    @Operation(
            summary = "Angel One OAuth callback",
            description = "Receives Angel One OAuth tokens, validates the signed state, and stores encrypted broker session tokens."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Angel One account connected",
                    content = @Content(mediaType = MediaType.TEXT_HTML_VALUE, schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Invalid or expired OAuth callback state", content = @Content)
    })
    public ResponseEntity<String> callback(
            @Parameter(description = "Angel One JWT/access token returned by the OAuth flow", required = true)
            @RequestParam("auth_token") String authToken,
            @Parameter(description = "Angel One feed token returned by the OAuth flow", required = true)
            @RequestParam("feed_token") String feedToken,
            @Parameter(description = "Optional Angel One refresh token when returned by the OAuth flow")
            @RequestParam(value = "refresh_token", required = false) String refreshToken,
            @Parameter(description = "Signed ExecuteMe OAuth state", required = true)
            @RequestParam("state") String state
    ) {
        long telegramUserId = oauthStateService.validateState(state);
        TelegramUser user = telegramUserService.getRequiredByTelegramUserId(telegramUserId);
        BrokerSession session = brokerSessionService.storeAngelSession(user.getId(), authToken, refreshToken, feedToken);

        String body = """
                <!doctype html>
                <html lang="en">
                <head><meta charset="utf-8"><title>Angel One Connected</title></head>
                <body>
                    <h1>Angel One connected</h1>
                    <p>Your broker account %s is now linked for today's trading session. You may close this page.</p>
                </body>
                </html>
                """.formatted(escapeHtml(session.getBrokerClientId()));
        return ResponseEntity.ok(body);
    }

    private static String escapeHtml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
