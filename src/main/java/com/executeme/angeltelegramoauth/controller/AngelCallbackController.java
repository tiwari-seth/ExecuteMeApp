package com.executeme.angeltelegramoauth.controller;

import com.executeme.angeltelegramoauth.domain.BrokerSession;
import com.executeme.angeltelegramoauth.service.BrokerSessionService;
import com.executeme.angeltelegramoauth.service.OAuthService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AngelCallbackController {

    private final OAuthService oauthService;
    private final BrokerSessionService brokerSessionService;

    public AngelCallbackController(OAuthService oauthService, BrokerSessionService brokerSessionService) {
        this.oauthService = oauthService;
        this.brokerSessionService = brokerSessionService;
    }

    @GetMapping(value = "/broker/angel/callback", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> callback(@RequestParam("auth_token") String authToken,
                                           @RequestParam("feed_token") String feedToken,
                                           @RequestParam("state") String state) {
        long telegramUserId = oauthService.validateState(state);
        BrokerSession session = brokerSessionService.storeAngelSession(telegramUserId, authToken, feedToken);

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
