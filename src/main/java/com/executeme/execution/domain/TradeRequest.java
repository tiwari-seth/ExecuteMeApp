package com.executeme.execution.domain;

import com.executeme.execution.model.OrderType;
import com.executeme.execution.model.OrderDuration;
import com.executeme.execution.model.OrderVariety;
import com.executeme.execution.model.ProductType;
import com.executeme.execution.model.TradeRequestStatus;
import com.executeme.execution.model.TransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.math.BigDecimal;

@Entity
@Table(name = "trade_requests")
public class TradeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "symbol", nullable = false, length = 100)
    private String symbol;

    @Column(name = "symbol_token", nullable = false, length = 100)
    private String symbolToken;

    @Column(name = "exchange", nullable = false, length = 30)
    private String exchange;

    @Enumerated(EnumType.STRING)
    @Column(name = "variety", nullable = false, length = 30)
    private OrderVariety variety = OrderVariety.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 30)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, length = 30)
    private OrderType orderType;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "duration", nullable = false, length = 30)
    private OrderDuration duration = OrderDuration.DAY;

    @Column(name = "price", nullable = false, precision = 18, scale = 4)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(name = "trigger_price", precision = 18, scale = 4)
    private BigDecimal triggerPrice;

    @Column(name = "square_off", precision = 18, scale = 4)
    private BigDecimal squareOff = BigDecimal.ZERO;

    @Column(name = "stop_loss", precision = 18, scale = 4)
    private BigDecimal stopLoss = BigDecimal.ZERO;

    @Column(name = "scrip_consent", length = 10)
    private String scripConsent;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false, length = 30)
    private ProductType productType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private TradeRequestStatus status = TradeRequestStatus.SUBMITTED;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected TradeRequest() {
    }

    public TradeRequest(String symbol, String symbolToken, String exchange, OrderVariety variety,
                        TransactionType transactionType, OrderType orderType, int quantity, OrderDuration duration,
                        BigDecimal price, BigDecimal triggerPrice, BigDecimal squareOff, BigDecimal stopLoss,
                        String scripConsent, ProductType productType, String createdBy) {
        this.symbol = symbol;
        this.symbolToken = symbolToken;
        this.exchange = exchange;
        this.variety = variety == null ? OrderVariety.NORMAL : variety;
        this.transactionType = transactionType;
        this.orderType = orderType;
        this.quantity = quantity;
        this.duration = duration == null ? OrderDuration.DAY : duration;
        this.price = price == null ? BigDecimal.ZERO : price;
        this.triggerPrice = triggerPrice;
        this.squareOff = squareOff == null ? BigDecimal.ZERO : squareOff;
        this.stopLoss = stopLoss == null ? BigDecimal.ZERO : stopLoss;
        this.scripConsent = scripConsent;
        this.productType = productType;
        this.createdBy = createdBy;
    }

    public Long getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getSymbolToken() {
        return symbolToken;
    }

    public String getExchange() {
        return exchange;
    }

    public OrderVariety getVariety() {
        return variety;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public int getQuantity() {
        return quantity;
    }

    public OrderDuration getDuration() {
        return duration;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getTriggerPrice() {
        return triggerPrice;
    }

    public BigDecimal getSquareOff() {
        return squareOff;
    }

    public BigDecimal getStopLoss() {
        return stopLoss;
    }

    public String getScripConsent() {
        return scripConsent;
    }

    public ProductType getProductType() {
        return productType;
    }

    public TradeRequestStatus getStatus() {
        return status;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void markProcessing() {
        status = TradeRequestStatus.PROCESSING;
    }

    public void markCompleted(TradeRequestStatus status) {
        this.status = status;
    }
}
