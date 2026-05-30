package com.executeme.broker.session.repository;

import com.executeme.broker.session.domain.BrokerSession;
import com.executeme.broker.session.domain.BrokerSessionStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface BrokerSessionRepository extends JpaRepository<BrokerSession, Long> {

    Optional<BrokerSession> findByUserIdAndBrokerNameAndBrokerClientId(
            Long userId,
            String brokerName,
            String brokerClientId
    );

    List<BrokerSession> findByBrokerNameOrderByUpdatedAtDesc(String brokerName);

    List<BrokerSession> findByBrokerNameAndStatus(String brokerName, BrokerSessionStatus status);

    List<BrokerSession> findByBrokerNameAndStatusAndUserIdIn(
            String brokerName,
            BrokerSessionStatus status,
            List<Long> userIds
    );

    @Modifying
    @Query("""
            update BrokerSession session
            set session.status = com.executeme.broker.session.domain.BrokerSessionStatus.EXPIRED,
                session.updatedAt = :now
            where session.status = com.executeme.broker.session.domain.BrokerSessionStatus.ACTIVE
              and session.tokenExpiresAt is not null
              and session.tokenExpiresAt <= :now
            """)
    int expireActiveSessions(@Param("now") Instant now);
}
