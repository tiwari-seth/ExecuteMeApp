package com.executeme.angeltelegramoauth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "broker_sessions")
public class BrokerSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "telegram_user_id", nullable = false)
    private Long telegramUserId;

    @Column(name = "broker_name", nullable = false, length = 50)
    private String brokerName;

    @Column(name = "broker_client_id", nullable = false, length = 100)
    private String brokerClientId;

    @Column(name = "auth_token", nullable = false, columnDefinition = "TEXT")
    private String encryptedAuthToken;

    @Column(name = "feed_token", nullable = false, columnDefinition = "TEXT")
    private String encryptedFeedToken;

    @Column(name = "token_expiry")
    private Instant tokenExpiry;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected BrokerSession() {
    }

    public BrokerSession(Long telegramUserId, String brokerName, String brokerClientId,
                         String encryptedAuthToken, String encryptedFeedToken, Instant tokenExpiry) {
        this.telegramUserId = telegramUserId;
        this.brokerName = brokerName;
        this.brokerClientId = brokerClientId;
        this.encryptedAuthToken = encryptedAuthToken;
        this.encryptedFeedToken = encryptedFeedToken;
        this.tokenExpiry = tokenExpiry;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getTelegramUserId() {
        return telegramUserId;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public String getBrokerClientId() {
        return brokerClientId;
    }

    public Instant getTokenExpiry() {
        return tokenExpiry;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void replaceTokens(String brokerClientId, String encryptedAuthToken,
                              String encryptedFeedToken, Instant tokenExpiry) {
        this.brokerClientId = brokerClientId;
        this.encryptedAuthToken = encryptedAuthToken;
        this.encryptedFeedToken = encryptedFeedToken;
        this.tokenExpiry = tokenExpiry;
        this.updatedAt = Instant.now();
    }
}
