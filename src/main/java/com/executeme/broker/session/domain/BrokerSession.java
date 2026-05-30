package com.executeme.broker.session.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "broker_name", nullable = false, length = 50)
    private String brokerName;

    @Column(name = "broker_client_id", nullable = false, length = 100)
    private String brokerClientId;

    @Column(name = "access_token_encrypted", nullable = false, columnDefinition = "TEXT")
    private String encryptedAccessToken;

    @Column(name = "refresh_token_encrypted", columnDefinition = "TEXT")
    private String encryptedRefreshToken;

    @Column(name = "feed_token_encrypted", nullable = false, columnDefinition = "TEXT")
    private String encryptedFeedToken;

    @Column(name = "token_generated_at", nullable = false)
    private Instant tokenGeneratedAt = Instant.now();

    @Column(name = "token_expires_at")
    private Instant tokenExpiresAt;

    @Column(name = "last_login_at", nullable = false)
    private Instant lastLoginAt = Instant.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private BrokerSessionStatus status = BrokerSessionStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected BrokerSession() {
    }

    public BrokerSession(Long userId, String brokerName, String brokerClientId,
                         String encryptedAccessToken, String encryptedRefreshToken, String encryptedFeedToken,
                         Instant tokenGeneratedAt, Instant tokenExpiresAt) {
        this.userId = userId;
        this.brokerName = brokerName;
        this.brokerClientId = brokerClientId;
        this.encryptedAccessToken = encryptedAccessToken;
        this.encryptedRefreshToken = encryptedRefreshToken;
        this.encryptedFeedToken = encryptedFeedToken;
        this.tokenGeneratedAt = tokenGeneratedAt;
        this.tokenExpiresAt = tokenExpiresAt;
        this.lastLoginAt = tokenGeneratedAt;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public String getBrokerClientId() {
        return brokerClientId;
    }

    public String getEncryptedAccessToken() {
        return encryptedAccessToken;
    }

    public String getEncryptedRefreshToken() {
        return encryptedRefreshToken;
    }

    public String getEncryptedFeedToken() {
        return encryptedFeedToken;
    }

    public Instant getTokenGeneratedAt() {
        return tokenGeneratedAt;
    }

    public Instant getTokenExpiresAt() {
        return tokenExpiresAt;
    }

    public BrokerSessionStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public void replaceTokens(String encryptedAccessToken, String encryptedRefreshToken,
                              String encryptedFeedToken, Instant tokenGeneratedAt, Instant tokenExpiresAt) {
        this.encryptedAccessToken = encryptedAccessToken;
        this.encryptedRefreshToken = encryptedRefreshToken;
        this.encryptedFeedToken = encryptedFeedToken;
        this.tokenGeneratedAt = tokenGeneratedAt;
        this.tokenExpiresAt = tokenExpiresAt;
        this.lastLoginAt = tokenGeneratedAt;
        this.status = BrokerSessionStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void markExpired() {
        this.status = BrokerSessionStatus.EXPIRED;
        this.updatedAt = Instant.now();
    }

    public boolean isExpiredAt(Instant now) {
        return tokenExpiresAt != null && !tokenExpiresAt.isAfter(now);
    }
}
