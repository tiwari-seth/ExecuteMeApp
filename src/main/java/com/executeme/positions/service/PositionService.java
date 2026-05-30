package com.executeme.positions.service;

import com.executeme.broker.client.BrokerClient;
import com.executeme.broker.model.BrokerPosition;
import com.executeme.broker.session.domain.BrokerSession;
import com.executeme.broker.session.service.BrokerSessionService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PositionService {

    private final BrokerSessionService brokerSessionService;
    private final BrokerClient brokerClient;

    public PositionService(BrokerSessionService brokerSessionService, BrokerClient brokerClient) {
        this.brokerSessionService = brokerSessionService;
        this.brokerClient = brokerClient;
    }

    public List<BrokerPosition> allAngelPositions() {
        return brokerSessionService.listExecutableAngelSessions(List.of()).stream()
                .map(BrokerSession::getUserId)
                .flatMap(userId -> brokerClient.getPositions(userId.toString()).stream())
                .toList();
    }
}
