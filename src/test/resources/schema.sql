-- 테스트용 H2 데이터베이스 스키마
-- Phase 2.3: MyBatis Mapper 테스트를 위한 H2 스키마

-- 1. 사용자 테이블 (users)
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NULL,
    name VARCHAR(100) NOT NULL,
    auth_provider VARCHAR(20) DEFAULT 'LOCAL' NOT NULL,
    role VARCHAR(20) DEFAULT 'USER' NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    profile_image_url VARCHAR(500) NULL,
    last_login_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. 자산 테이블 (assets)
CREATE TABLE assets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    symbol VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    asset_type VARCHAR(20) NOT NULL,
    exchange VARCHAR(50) NOT NULL,
    country_code VARCHAR(3) DEFAULT 'KR',
    quantity DECIMAL(18,8) NOT NULL DEFAULT 0,
    average_price DECIMAL(18,2) NOT NULL DEFAULT 0,
    currency VARCHAR(10) DEFAULT 'KRW',
    is_active BOOLEAN DEFAULT TRUE,
    notes TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- 외래키
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- 유니크 제약조건
    UNIQUE(user_id, symbol, exchange)
);

-- 3. 거래 내역 테이블 (transactions)
CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    asset_id BIGINT NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    quantity DECIMAL(18,8) NOT NULL,
    price DECIMAL(18,2) NOT NULL,
    total_amount DECIMAL(18,2) NOT NULL,
    fee DECIMAL(18,2) DEFAULT 0,
    tax DECIMAL(18,2) DEFAULT 0,
    net_amount DECIMAL(18,2) NOT NULL,
    transaction_date TIMESTAMP NOT NULL,
    notes TEXT NULL,
    external_id VARCHAR(100) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- 외래키
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (asset_id) REFERENCES assets(id) ON DELETE CASCADE
);

-- 4. 가격 히스토리 테이블 (price_history)
CREATE TABLE price_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    symbol VARCHAR(50) NOT NULL,
    exchange VARCHAR(50) NOT NULL,
    price DECIMAL(18,2) NOT NULL,
    volume DECIMAL(18,8) DEFAULT 0,
    market_cap DECIMAL(20,2) NULL,
    high_price DECIMAL(18,2) NULL,
    low_price DECIMAL(18,2) NULL,
    open_price DECIMAL(18,2) NULL,
    close_price DECIMAL(18,2) NULL,
    change_rate DECIMAL(5,2) NULL,
    data_source VARCHAR(20) NOT NULL,
    price_timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- 유니크 제약조건
    UNIQUE(symbol, exchange, price_timestamp)
);

-- 5. API 키 테이블 (api_keys)
CREATE TABLE api_keys (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    exchange_type VARCHAR(20) NOT NULL,
    exchange_name VARCHAR(50) NOT NULL,
    access_key TEXT NOT NULL,
    secret_key TEXT NOT NULL,
    api_permissions TEXT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    last_used_at TIMESTAMP NULL,
    expires_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- 외래키
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- 유니크 제약조건
    UNIQUE(user_id, exchange_type)
);

-- 6. 포트폴리오 스냅샷 테이블 (portfolio_snapshots)
CREATE TABLE portfolio_snapshots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    snapshot_date DATE NOT NULL,
    total_investment DECIMAL(20,2) NOT NULL DEFAULT 0,
    total_current_value DECIMAL(20,2) NOT NULL DEFAULT 0,
    total_profit_loss DECIMAL(20,2) NOT NULL DEFAULT 0,
    profit_rate DECIMAL(8,4) NOT NULL DEFAULT 0,
    asset_count INT NOT NULL DEFAULT 0,
    crypto_value DECIMAL(20,2) DEFAULT 0,
    stock_value DECIMAL(20,2) DEFAULT 0,
    notes TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- 외래키
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- 유니크 제약조건
    UNIQUE(user_id, snapshot_date)
);

-- 인덱스 생성 (성능 최적화용)
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_auth_provider ON users(auth_provider);
CREATE INDEX idx_users_is_active ON users(is_active);

CREATE INDEX idx_assets_user_id ON assets(user_id);
CREATE INDEX idx_assets_symbol ON assets(symbol);
CREATE INDEX idx_assets_asset_type ON assets(asset_type);
CREATE INDEX idx_assets_exchange ON assets(exchange);
CREATE INDEX idx_assets_is_active ON assets(is_active);

CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_asset_id ON transactions(asset_id);
CREATE INDEX idx_transactions_transaction_type ON transactions(transaction_type);
CREATE INDEX idx_transactions_transaction_date ON transactions(transaction_date);

CREATE INDEX idx_price_history_symbol ON price_history(symbol);
CREATE INDEX idx_price_history_exchange ON price_history(exchange);
CREATE INDEX idx_price_history_price_timestamp ON price_history(price_timestamp);

CREATE INDEX idx_api_keys_user_id ON api_keys(user_id);
CREATE INDEX idx_api_keys_exchange_type ON api_keys(exchange_type);
CREATE INDEX idx_api_keys_is_active ON api_keys(is_active);

CREATE INDEX idx_portfolio_snapshots_user_id ON portfolio_snapshots(user_id);
CREATE INDEX idx_portfolio_snapshots_snapshot_date ON portfolio_snapshots(snapshot_date);