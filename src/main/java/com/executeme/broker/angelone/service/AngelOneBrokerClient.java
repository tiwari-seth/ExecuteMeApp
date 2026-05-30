package com.executeme.broker.angelone.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.executeme.broker.angelone.config.SmartApiProperties;
import com.executeme.broker.client.BrokerClient;
import com.executeme.broker.model.BrokerPosition;
import com.executeme.broker.model.ExitPositionRequest;
import com.executeme.broker.model.ExitPositionResponse;
import com.executeme.broker.model.OrderRequest;
import com.executeme.broker.model.OrderResponse;
import com.executeme.broker.model.OrderStatusResponse;
import com.executeme.broker.model.TokenRefreshResponse;
import com.executeme.broker.model.TokenValidationResponse;
import com.executeme.broker.session.service.BrokerSessionCredentials;
import com.executeme.broker.session.service.BrokerSessionService;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
public class AngelOneBrokerClient implements BrokerClient {

    private static final String PLACE_ORDER_PATH = "/rest/secure/angelbroking/order/v1/placeOrder";

    private final BrokerSessionService brokerSessionService;
    private final SmartApiProperties smartApiProperties;
    private final RestClient restClient;

    public AngelOneBrokerClient(BrokerSessionService brokerSessionService, SmartApiProperties smartApiProperties) {
        this.brokerSessionService = brokerSessionService;
        this.smartApiProperties = smartApiProperties;
        this.restClient = RestClient.builder()
                .baseUrl(smartApiProperties.rootUrl())
                .build();
    }

    @Override
    public OrderResponse placeOrder(OrderRequest request) {
        try {
            BrokerSessionCredentials credentials = brokerSessionService.getActiveCredentials(request.brokerSessionId());
            JsonNode response = restClient.post()
                    .uri(PLACE_ORDER_PATH)
                    .headers(headers -> applySmartApiHeaders(headers, credentials.accessToken()))
                    .body(orderPayload(request))
                    .retrieve()
                    .body(JsonNode.class);
            return parseOrderResponse(response);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 401 || ex.getStatusCode().value() == 403) {
                brokerSessionService.markExpired(request.brokerSessionId());
            }
            return new OrderResponse(false, null,
                    "Angel One order API returned HTTP " + ex.getStatusCode().value() + ": " + ex.getResponseBodyAsString());
        } catch (RuntimeException ex) {
            return new OrderResponse(false, null, ex.getMessage());
        }
    }

    @Override
    public ExitPositionResponse exitPosition(ExitPositionRequest request) {
        return new ExitPositionResponse(false, null,
                "Angel One exit-position adapter is not wired to SmartAPI order endpoints yet");
    }

    @Override
    public List<BrokerPosition> getPositions(String userId) {
        return List.of();
    }

    @Override
    public TokenValidationResponse validateSession(String userId) {
        return new TokenValidationResponse(true, "ACTIVE", "Session exists locally");
    }

    @Override
    public TokenRefreshResponse refreshSession(String userId) {
        return new TokenRefreshResponse(false, "Refresh is not implemented for Angel One yet");
    }

    @Override
    public OrderStatusResponse getOrderStatus(String orderId) {
        return new OrderStatusResponse(orderId, "UNKNOWN",
                "Angel One order-status adapter is not wired to SmartAPI order endpoints yet");
    }

    private void applySmartApiHeaders(org.springframework.http.HttpHeaders headers, String accessToken) {
        headers.setBearerAuth(accessToken);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(org.springframework.http.MediaType.APPLICATION_JSON));
        headers.set("X-PrivateKey", required(smartApiProperties.apiKey(), "SMARTAPI_API_KEY"));
        headers.set("X-UserType", smartApiProperties.userType());
        headers.set("X-SourceID", smartApiProperties.sourceId());
        headers.set("X-ClientLocalIP", smartApiProperties.clientLocalIp());
        headers.set("X-ClientPublicIP", smartApiProperties.clientPublicIp());
        headers.set("X-MACAddress", smartApiProperties.macAddress());
    }

    private Map<String, String> orderPayload(OrderRequest request) {
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("variety", defaultValue(request.variety(), "NORMAL"));
        payload.put("tradingsymbol", request.symbol());
        payload.put("symboltoken", request.symbolToken());
        payload.put("transactiontype", request.transactionType());
        payload.put("exchange", request.exchange());
        payload.put("ordertype", request.orderType());
        payload.put("producttype", request.productType());
        payload.put("duration", defaultValue(request.duration(), "DAY"));
        payload.put("price", decimalValue(request.price(), BigDecimal.ZERO));
        payload.put("squareoff", decimalValue(request.squareOff(), BigDecimal.ZERO));
        payload.put("stoploss", decimalValue(request.stopLoss(), BigDecimal.ZERO));
        payload.put("quantity", Integer.toString(request.quantity()));
        if (request.triggerPrice() != null) {
            payload.put("triggerprice", decimalValue(request.triggerPrice(), BigDecimal.ZERO));
        }
        if (request.scripConsent() != null && !request.scripConsent().isBlank()) {
            payload.put("scripconsent", request.scripConsent());
        }
        return payload;
    }

    private OrderResponse parseOrderResponse(JsonNode response) {
        if (response == null) {
            return new OrderResponse(false, null, "Angel One returned an empty response");
        }
        boolean success = response.path("status").asBoolean(false);
        String message = response.path("message").asText(null);
        String orderId = response.path("data").path("orderid").asText(null);
        if (success && orderId != null && !orderId.isBlank()) {
            return new OrderResponse(true, orderId, message);
        }
        String errorCode = response.path("errorcode").asText(null);
        String failureMessage = message == null || message.isBlank() ? "Angel One order placement failed" : message;
        if (errorCode != null && !errorCode.isBlank()) {
            failureMessage = failureMessage + " (" + errorCode + ")";
        }
        return new OrderResponse(false, orderId, failureMessage);
    }

    private static String defaultValue(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static String decimalValue(BigDecimal value, BigDecimal defaultValue) {
        BigDecimal resolved = value == null ? defaultValue : value;
        return resolved.stripTrailingZeros().toPlainString();
    }

    private static String required(String value, String envName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(envName + " must be configured");
        }
        return value;
    }
}
