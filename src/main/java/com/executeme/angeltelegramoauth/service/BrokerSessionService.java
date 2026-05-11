package com.executeme.angeltelegramoauth.service;

import com.executeme.angeltelegramoauth.domain.BrokerSession;
import com.executeme.angeltelegramoauth.repository.BrokerSessionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BrokerSessionService {

    public static final String ANGEL_ONE = "ANGEL_ONE";
    private final BrokerSessionRepository brokerSessionRepository;
    private final EncryptionService encryptionService;
    private final SmartApiTokenService smartApiTokenService;

    public BrokerSessionService(BrokerSessionRepository brokerSessionRepository,
                                EncryptionService encryptionService,
                                SmartApiTokenService smartApiTokenService) {
        this.brokerSessionRepository = brokerSessionRepository;
        this.encryptionService = encryptionService;
        this.smartApiTokenService = smartApiTokenService;
    }

    @Transactional
    public BrokerSession storeAngelSession(long telegramUserId, String authToken, String feedToken) {
        SmartApiTokenService.AngelTokenDetails details = smartApiTokenService.decode(authToken);
        String encryptedAuthToken = encryptionService.encrypt(authToken);
        String encryptedFeedToken = encryptionService.encrypt(feedToken);

        BrokerSession session = brokerSessionRepository
                .findByTelegramUserIdAndBrokerName(telegramUserId, ANGEL_ONE)
                .map(existing -> {
                    existing.replaceTokens(details.brokerClientId(), encryptedAuthToken,
                            encryptedFeedToken, details.expiresAt());
                    return existing;
                })
                .orElseGet(() -> new BrokerSession(telegramUserId, ANGEL_ONE, details.brokerClientId(),
                        encryptedAuthToken, encryptedFeedToken, details.expiresAt()));

        return brokerSessionRepository.save(session);
    }

    @Transactional(readOnly = true)
    public List<BrokerSession> listAngelSessions() {
        return brokerSessionRepository.findByBrokerNameOrderByUpdatedAtDesc(ANGEL_ONE);
    }
}
