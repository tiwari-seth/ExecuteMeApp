package com.executeme.angeltelegramoauth.repository;

import com.executeme.angeltelegramoauth.domain.BrokerSession;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrokerSessionRepository extends JpaRepository<BrokerSession, Long> {

    Optional<BrokerSession> findByTelegramUserIdAndBrokerName(Long telegramUserId, String brokerName);

    List<BrokerSession> findByBrokerNameOrderByUpdatedAtDesc(String brokerName);
}
