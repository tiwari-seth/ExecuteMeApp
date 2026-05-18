CREATE TABLE IF NOT EXISTS telegram_users (
    id BIGSERIAL PRIMARY KEY,
    telegram_user_id BIGINT UNIQUE NOT NULL,
    telegram_username VARCHAR(255),
    full_name VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS broker_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES telegram_users(id),
    broker_name VARCHAR(50) NOT NULL,
    broker_client_id VARCHAR(100) NOT NULL,
    access_token_encrypted TEXT NOT NULL,
    refresh_token_encrypted TEXT,
    feed_token_encrypted TEXT NOT NULL,
    token_generated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    token_expires_at TIMESTAMP,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    last_login_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_broker_sessions_user_broker_client
    ON broker_sessions (user_id, broker_name, broker_client_id);

CREATE INDEX IF NOT EXISTS ix_broker_sessions_status_expiry
    ON broker_sessions (status, token_expires_at);

CREATE TABLE IF NOT EXISTS trade_requests (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(100) NOT NULL,
    symbol_token VARCHAR(100) NOT NULL,
    exchange VARCHAR(30) NOT NULL,
    variety VARCHAR(30) NOT NULL DEFAULT 'NORMAL',
    transaction_type VARCHAR(30) NOT NULL,
    order_type VARCHAR(30) NOT NULL,
    quantity INTEGER NOT NULL,
    duration VARCHAR(30) NOT NULL DEFAULT 'DAY',
    price DECIMAL(18, 4) NOT NULL DEFAULT 0,
    trigger_price DECIMAL(18, 4),
    square_off DECIMAL(18, 4) NOT NULL DEFAULT 0,
    stop_loss DECIMAL(18, 4) NOT NULL DEFAULT 0,
    scrip_consent VARCHAR(10),
    product_type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS trade_executions (
    id BIGSERIAL PRIMARY KEY,
    trade_request_id BIGINT NOT NULL REFERENCES trade_requests(id),
    user_id BIGINT NOT NULL REFERENCES telegram_users(id),
    broker_session_id BIGINT NOT NULL REFERENCES broker_sessions(id),
    broker_name VARCHAR(50) NOT NULL,
    broker_client_id VARCHAR(100) NOT NULL,
    status VARCHAR(30) NOT NULL,
    broker_order_id VARCHAR(100),
    failure_reason TEXT,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    processing_started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS ix_trade_executions_request
    ON trade_executions (trade_request_id);

CREATE INDEX IF NOT EXISTS ix_trade_executions_broker_session
    ON trade_executions (broker_session_id);

CREATE INDEX IF NOT EXISTS ix_trade_executions_status_created
    ON trade_executions (status, created_at);
