package com.executeme.admin;

import com.executeme.broker.session.domain.BrokerSession;
import com.executeme.users.domain.TelegramUser;
import com.executeme.broker.session.service.BrokerSessionService;
import com.executeme.users.service.TelegramUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@Tag(name = "Admin Monitoring")
@SecurityRequirement(name = "basicAuth")
public class AdminController {

    private final BrokerSessionService brokerSessionService;
    private final TelegramUserService telegramUserService;

    public AdminController(BrokerSessionService brokerSessionService, TelegramUserService telegramUserService) {
        this.brokerSessionService = brokerSessionService;
        this.telegramUserService = telegramUserService;
    }

    @GetMapping("/broker-sessions")
    @Operation(
            summary = "List broker sessions",
            description = "Returns broker session metadata for connected Angel One users without exposing encrypted tokens."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Broker sessions returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = BrokerSessionView.class)))),
            @ApiResponse(responseCode = "401", description = "Admin authentication is required", content = @Content),
            @ApiResponse(responseCode = "403", description = "Admin APIs are disabled", content = @Content)
    })
    public List<BrokerSessionView> brokerSessions() {
        return brokerSessionService.listAngelSessions().stream()
                .map(BrokerSessionView::from)
                .toList();
    }

    @GetMapping("/telegram-users")
    @Operation(
            summary = "List Telegram users",
            description = "Returns active Telegram users registered through the Telegram bot allowlist/login flow."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Telegram users returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TelegramUserView.class)))),
            @ApiResponse(responseCode = "401", description = "Admin authentication is required", content = @Content),
            @ApiResponse(responseCode = "403", description = "Admin APIs are disabled", content = @Content)
    })
    public List<TelegramUserView> telegramUsers() {
        return telegramUserService.listActiveUsers().stream()
                .map(TelegramUserView::from)
                .toList();
    }

    @Schema(name = "BrokerSessionView", description = "Broker connection metadata visible to admins. Token values are never exposed.")
    public record BrokerSessionView(
            @Schema(description = "Broker session primary key", example = "12")
            Long id,
            @Schema(description = "Internal ExecuteMe user id", example = "7")
            Long userId,
            @Schema(description = "Broker code", example = "ANGEL_ONE")
            String brokerName,
            @Schema(description = "Broker-side client/account id", example = "A123456")
            String brokerClientId,
            @Schema(description = "When the current tokens were generated")
            Instant tokenGeneratedAt,
            @Schema(description = "When the current access token expires")
            Instant tokenExpiresAt,
            @Schema(description = "Session lifecycle status", example = "ACTIVE")
            String status,
            @Schema(description = "Last successful broker login time")
            Instant lastLoginAt,
            @Schema(description = "Session row creation time")
            Instant createdAt,
            @Schema(description = "Session row last update time")
            Instant updatedAt
    ) {
        static BrokerSessionView from(BrokerSession session) {
            return new BrokerSessionView(
                    session.getId(),
                    session.getUserId(),
                    session.getBrokerName(),
                    session.getBrokerClientId(),
                    session.getTokenGeneratedAt(),
                    session.getTokenExpiresAt(),
                    session.getStatus().name(),
                    session.getLastLoginAt(),
                    session.getCreatedAt(),
                    session.getUpdatedAt()
            );
        }
    }

    @Schema(name = "TelegramUserView", description = "Telegram user visible to admins.")
    public record TelegramUserView(
            @Schema(description = "Internal ExecuteMe user id", example = "7")
            Long id,
            @Schema(description = "Telegram platform user id", example = "987654321")
            Long telegramUserId,
            @Schema(description = "Telegram username", example = "trader_user")
            String telegramUsername,
            @Schema(description = "Telegram display name", example = "Trader User")
            String fullName,
            @Schema(description = "Whether this user is currently active")
            boolean active,
            @Schema(description = "User creation time")
            Instant createdAt
    ) {
        static TelegramUserView from(TelegramUser user) {
            return new TelegramUserView(
                    user.getId(),
                    user.getTelegramUserId(),
                    user.getTelegramUsername(),
                    user.getFullName(),
                    user.isActive(),
                    user.getCreatedAt()
            );
        }
    }
}
