package com.executeme.angeltelegramoauth.controller;

import com.executeme.angeltelegramoauth.domain.BrokerSession;
import com.executeme.angeltelegramoauth.domain.TelegramUser;
import com.executeme.angeltelegramoauth.service.BrokerSessionService;
import com.executeme.angeltelegramoauth.service.TelegramUserService;
import java.time.Instant;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final BrokerSessionService brokerSessionService;
    private final TelegramUserService telegramUserService;

    public AdminController(BrokerSessionService brokerSessionService, TelegramUserService telegramUserService) {
        this.brokerSessionService = brokerSessionService;
        this.telegramUserService = telegramUserService;
    }

    @GetMapping("/broker-sessions")
    public List<BrokerSessionView> brokerSessions() {
        return brokerSessionService.listAngelSessions().stream()
                .map(BrokerSessionView::from)
                .toList();
    }

    @GetMapping("/telegram-users")
    public List<TelegramUserView> telegramUsers() {
        return telegramUserService.listActiveUsers().stream()
                .map(TelegramUserView::from)
                .toList();
    }

    public record BrokerSessionView(
            Long id,
            Long telegramUserId,
            String brokerName,
            String brokerClientId,
            Instant tokenExpiry,
            Instant createdAt,
            Instant updatedAt
    ) {
        static BrokerSessionView from(BrokerSession session) {
            return new BrokerSessionView(
                    session.getId(),
                    session.getTelegramUserId(),
                    session.getBrokerName(),
                    session.getBrokerClientId(),
                    session.getTokenExpiry(),
                    session.getCreatedAt(),
                    session.getUpdatedAt()
            );
        }
    }

    public record TelegramUserView(
            Long id,
            Long telegramUserId,
            String telegramUsername,
            String fullName,
            boolean active,
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
