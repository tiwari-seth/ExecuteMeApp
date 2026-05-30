package com.executeme.broker.session.service;

import com.executeme.broker.session.domain.BrokerSession;
import com.executeme.broker.session.domain.BrokerSessionStatus;
import com.executeme.broker.session.repository.BrokerSessionRepository;
import com.executeme.broker.angelone.service.SmartApiTokenService;
import com.executeme.common.crypto.EncryptionService;
import java.time.Instant;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
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
    public BrokerSession storeAngelSession(long userId, String accessToken, String refreshToken, String feedToken) {
        SmartApiTokenService.AngelTokenDetails details = smartApiTokenService.decode(accessToken);
        Instant now = Instant.now();
        String encryptedAccessToken = encryptionService.encrypt(accessToken);
        String encryptedRefreshToken = encryptNullable(refreshToken);
        String encryptedFeedToken = encryptionService.encrypt(feedToken);

        BrokerSession session = brokerSessionRepository
                .findByUserIdAndBrokerNameAndBrokerClientId(userId, ANGEL_ONE, details.brokerClientId())
                .map(existing -> {
                    existing.replaceTokens(encryptedAccessToken, encryptedRefreshToken,
                            encryptedFeedToken, now, details.expiresAt());
                    return existing;
                })
                .orElseGet(() -> new BrokerSession(userId, ANGEL_ONE, details.brokerClientId(),
                        encryptedAccessToken, encryptedRefreshToken, encryptedFeedToken, now, details.expiresAt()));

        return brokerSessionRepository.save(session);
    }

    @Transactional(readOnly = true)
    public List<BrokerSession> listAngelSessions() {
        return brokerSessionRepository.findByBrokerNameOrderByUpdatedAtDesc(ANGEL_ONE);
    }

    @Transactional
    public List<BrokerSession> listExecutableAngelSessions(List<Long> userIds) {
        brokerSessionRepository.expireActiveSessions(Instant.now());
        if (userIds == null || userIds.isEmpty()) {
            return brokerSessionRepository.findByBrokerNameAndStatus(ANGEL_ONE, BrokerSessionStatus.ACTIVE);
        }
        return brokerSessionRepository.findByBrokerNameAndStatusAndUserIdIn(
                ANGEL_ONE,
                BrokerSessionStatus.ACTIVE,
                userIds
        );
    }

    @Transactional
    public BrokerSessionCredentials getActiveCredentials(Long brokerSessionId) {
        BrokerSession session = brokerSessionRepository.findById(brokerSessionId)
                .orElseThrow(() -> new IllegalArgumentException("Broker session not found: " + brokerSessionId));
        if (session.getStatus() != BrokerSessionStatus.ACTIVE) {
            throw new IllegalStateException("Broker session is not active: " + session.getStatus());
        }
        if (session.isExpiredAt(Instant.now())) {
            session.markExpired();
            throw new IllegalStateException("Broker session token is expired; login is required");
        }
        return new BrokerSessionCredentials(
                session.getId(),
                session.getUserId(),
                session.getBrokerName(),
                session.getBrokerClientId(),
                encryptionService.decrypt(session.getEncryptedAccessToken()),
                decryptNullable(session.getEncryptedRefreshToken()),
                encryptionService.decrypt(session.getEncryptedFeedToken()),
                session.getTokenExpiresAt()
        );
    }

    @Transactional
    public void markExpired(Long brokerSessionId) {
        brokerSessionRepository.findById(brokerSessionId).ifPresent(BrokerSession::markExpired);
    }

    @Scheduled(fixedDelayString = "${broker.sessions.expiry-scan-delay-ms:60000}")
    @Transactional
    public int expireActiveSessions() {
        return brokerSessionRepository.expireActiveSessions(Instant.now());
    }

    private String encryptNullable(String value) {
        return value == null || value.isBlank() ? null : encryptionService.encrypt(value);
    }

    private String decryptNullable(String value) {
        return value == null || value.isBlank() ? null : encryptionService.decrypt(value);
    }
}
