package com.executeme.broker.client;

import com.executeme.broker.model.BrokerPosition;
import com.executeme.broker.model.ExitPositionRequest;
import com.executeme.broker.model.ExitPositionResponse;
import com.executeme.broker.model.OrderRequest;
import com.executeme.broker.model.OrderResponse;
import com.executeme.broker.model.OrderStatusResponse;
import com.executeme.broker.model.TokenRefreshResponse;
import com.executeme.broker.model.TokenValidationResponse;
import java.util.List;

public interface BrokerClient {

    OrderResponse placeOrder(OrderRequest request);

    ExitPositionResponse exitPosition(ExitPositionRequest request);

    List<BrokerPosition> getPositions(String userId);

    TokenValidationResponse validateSession(String userId);

    TokenRefreshResponse refreshSession(String userId);

    OrderStatusResponse getOrderStatus(String orderId);
}
