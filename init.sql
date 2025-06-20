-- 포트폴리오 관리 시스템 초기 스키마
-- 생성일: 2025-06-19

-- 사용자 테이블
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NULL,
    name VARCHAR(100) NOT NULL,
    auth_provider ENUM('LOCAL', 'GOOGLE', 'KAKAO', 'NAVER') DEFAULT 'LOCAL',
    role ENUM('USER', 'ADMIN') DEFAULT 'USER',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 자산 테이블
CREATE TABLE IF NOT EXISTS assets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    symbol VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    asset_type ENUM('CRYPTO', 'STOCK') NOT NULL,
    exchange VARCHAR(50) NOT NULL,
    quantity DECIMAL(18,8) NOT NULL DEFAULT 0,
    average_price DECIMAL(18,2) NOT NULL DEFAULT 0,
    currency VARCHAR(10) DEFAULT 'KRW',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_symbol (user_id, symbol),
    INDEX idx_asset_type (asset_type)
);

-- 거래 내역 테이블
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    asset_id BIGINT NOT NULL,
    transaction_type ENUM('BUY', 'SELL') NOT NULL,
    quantity DECIMAL(18,8) NOT NULL,
    price DECIMAL(18,2) NOT NULL,
    total_amount DECIMAL(18,2) NOT NULL,
    fee DECIMAL(18,2) DEFAULT 0,
    transaction_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (asset_id) REFERENCES assets(id) ON DELETE CASCADE,
    INDEX idx_user_date (user_id, transaction_date),
    INDEX idx_asset_date (asset_id, transaction_date)
);

-- 가격 히스토리 테이블
CREATE TABLE IF NOT EXISTS price_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    symbol VARCHAR(50) NOT NULL,
    price DECIMAL(18,2) NOT NULL,
    volume DECIMAL(18,8) DEFAULT 0,
    source VARCHAR(20) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_symbol_timestamp (symbol, timestamp),
    INDEX idx_source_timestamp (source, timestamp)
);

-- 초기 테스트 데이터 삽입
INSERT INTO users (email, password, name, auth_provider) VALUES 
('test@example.com', '$2a$10$dummy.hash.for.testing.purposes.only', '테스트 사용자', 'LOCAL');

-- 테스트 자산 데이터
INSERT INTO assets (user_id, symbol, name, asset_type, exchange, quantity, average_price, currency) VALUES 
(1, 'BTC', '비트코인', 'CRYPTO', 'UPBIT', 0.5, 50000000, 'KRW'),
(1, '005930', '삼성전자', 'STOCK', 'KRX', 10, 75000, 'KRW');

-- 스키마 생성 완료 로그
SELECT 'Database schema created successfully!' as message;
