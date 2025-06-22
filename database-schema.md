# 🗄️ 데이터베이스 스키마 상세 설계

## 📋 **설계 원칙**

### **테이블 설계 원칙**
1. **명확한 컬럼명**: 축약 금지, 의미 명확
2. **일관된 명명 규칙**: snake_case 사용
3. **적절한 인덱스**: 성능 최적화
4. **외래키 제약조건**: 데이터 무결성 보장
5. **타임스탬프**: 모든 테이블에 created_at, updated_at

---

## 📊 **테이블 구조**

### **1. 사용자 테이블 (users)**

```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '사용자 고유 ID',
    email VARCHAR(255) UNIQUE NOT NULL COMMENT '이메일 (로그인 ID)',
    password VARCHAR(255) NULL COMMENT '비밀번호 (BCrypt 암호화, 소셜 로그인시 NULL)',
    name VARCHAR(100) NOT NULL COMMENT '사용자 이름',
    auth_provider ENUM('LOCAL', 'GOOGLE', 'KAKAO', 'NAVER') DEFAULT 'LOCAL' COMMENT '인증 제공자',
    role ENUM('USER', 'ADMIN') DEFAULT 'USER' COMMENT '사용자 권한',
    is_active BOOLEAN DEFAULT TRUE COMMENT '계정 활성화 여부',
    profile_image_url VARCHAR(500) NULL COMMENT '프로필 이미지 URL',
    last_login_at TIMESTAMP NULL COMMENT '마지막 로그인 시간',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '계정 생성 시간',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '정보 수정 시간',
    
    -- 인덱스
    INDEX idx_email (email),
    INDEX idx_auth_provider (auth_provider),
    INDEX idx_is_active (is_active),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 정보 테이블';
```

### **2. 자산 테이블 (assets)**

```sql
CREATE TABLE assets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '자산 고유 ID',
    user_id BIGINT NOT NULL COMMENT '사용자 ID (FK)',
    symbol VARCHAR(50) NOT NULL COMMENT '종목 코드 (예: BTC, 005930)',
    name VARCHAR(100) NOT NULL COMMENT '자산 이름 (예: 비트코인, 삼성전자)',
    asset_type ENUM('CRYPTO', 'STOCK', 'ETF', 'FUND') NOT NULL COMMENT '자산 유형',
    exchange VARCHAR(50) NOT NULL COMMENT '거래소/증권사 (예: UPBIT, KRX, NASDAQ)',
    country_code VARCHAR(3) DEFAULT 'KR' COMMENT '국가 코드 (KR, US, JP)',
    quantity DECIMAL(18,8) NOT NULL DEFAULT 0 COMMENT '보유 수량',
    average_price DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '평균 매수가',
    currency VARCHAR(10) DEFAULT 'KRW' COMMENT '통화 (KRW, USD, BTC)',
    is_active BOOLEAN DEFAULT TRUE COMMENT '보유 여부 (매도 완료시 FALSE)',
    notes TEXT NULL COMMENT '메모',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '자산 추가 시간',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '정보 수정 시간',
    
    -- 외래키
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- 인덱스
    UNIQUE KEY uk_user_symbol_exchange (user_id, symbol, exchange) COMMENT '동일 사용자의 동일 종목 중복 방지',
    INDEX idx_user_id (user_id),
    INDEX idx_symbol (symbol),
    INDEX idx_asset_type (asset_type),
    INDEX idx_exchange (exchange),
    INDEX idx_is_active (is_active),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='보유 자산 정보 테이블';
```

### **3. 거래 내역 테이블 (transactions)**

```sql
CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '거래 고유 ID',
    user_id BIGINT NOT NULL COMMENT '사용자 ID (FK)',
    asset_id BIGINT NOT NULL COMMENT '자산 ID (FK)',
    transaction_type ENUM('BUY', 'SELL', 'DIVIDEND', 'SPLIT') NOT NULL COMMENT '거래 유형',
    quantity DECIMAL(18,8) NOT NULL COMMENT '거래 수량',
    price DECIMAL(18,2) NOT NULL COMMENT '거래 단가',
    total_amount DECIMAL(18,2) NOT NULL COMMENT '거래 총액 (수량 × 단가)',
    fee DECIMAL(18,2) DEFAULT 0 COMMENT '거래 수수료',
    tax DECIMAL(18,2) DEFAULT 0 COMMENT '세금',
    net_amount DECIMAL(18,2) NOT NULL COMMENT '실제 거래금액 (총액 ± 수수료 ± 세금)',
    transaction_date TIMESTAMP NOT NULL COMMENT '거래 일시',
    notes TEXT NULL COMMENT '거래 메모',
    external_id VARCHAR(100) NULL COMMENT '외부 시스템 거래 ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '기록 생성 시간',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '기록 수정 시간',
    
    -- 외래키
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (asset_id) REFERENCES assets(id) ON DELETE CASCADE,
    
    -- 인덱스
    INDEX idx_user_id (user_id),
    INDEX idx_asset_id (asset_id),
    INDEX idx_transaction_type (transaction_type),
    INDEX idx_transaction_date (transaction_date),
    INDEX idx_user_date (user_id, transaction_date),
    INDEX idx_asset_date (asset_id, transaction_date),
    INDEX idx_external_id (external_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='거래 내역 테이블';
```

### **4. 가격 히스토리 테이블 (price_history)**

```sql
CREATE TABLE price_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '가격 데이터 고유 ID',
    symbol VARCHAR(50) NOT NULL COMMENT '종목 코드',
    exchange VARCHAR(50) NOT NULL COMMENT '거래소',
    price DECIMAL(18,2) NOT NULL COMMENT '해당 시점 가격',
    volume DECIMAL(18,8) DEFAULT 0 COMMENT '거래량',
    market_cap DECIMAL(20,2) NULL COMMENT '시가총액',
    high_price DECIMAL(18,2) NULL COMMENT '고가',
    low_price DECIMAL(18,2) NULL COMMENT '저가',
    open_price DECIMAL(18,2) NULL COMMENT '시가',
    close_price DECIMAL(18,2) NULL COMMENT '종가',
    change_rate DECIMAL(5,2) NULL COMMENT '전일 대비 변동률 (%)',
    data_source VARCHAR(20) NOT NULL COMMENT '데이터 출처 (UPBIT, YAHOO, ALPHA_VANTAGE)',
    price_timestamp TIMESTAMP NOT NULL COMMENT '가격 데이터 시점',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '데이터 수집 시간',
    
    -- 인덱스
    UNIQUE KEY uk_symbol_exchange_timestamp (symbol, exchange, price_timestamp) COMMENT '중복 데이터 방지',
    INDEX idx_symbol (symbol),
    INDEX idx_exchange (exchange),
    INDEX idx_data_source (data_source),
    INDEX idx_price_timestamp (price_timestamp),
    INDEX idx_symbol_timestamp (symbol, price_timestamp),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='가격 히스토리 테이블';
```

### **5. API 키 테이블 (api_keys)**

```sql
CREATE TABLE api_keys (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'API 키 고유 ID',
    user_id BIGINT NOT NULL COMMENT '사용자 ID (FK)',
    exchange_type ENUM('UPBIT', 'BITHUMB', 'KIWOOM', 'KIS') NOT NULL COMMENT '거래소/증권사 타입',
    exchange_name VARCHAR(50) NOT NULL COMMENT '거래소/증권사 이름',
    access_key TEXT NOT NULL COMMENT '액세스 키 (AES-256 암호화)',
    secret_key TEXT NOT NULL COMMENT '시크릿 키 (AES-256 암호화)',
    api_permissions JSON NULL COMMENT 'API 권한 정보',
    is_active BOOLEAN DEFAULT TRUE COMMENT '활성화 여부',
    last_used_at TIMESTAMP NULL COMMENT '마지막 사용 시간',
    expires_at TIMESTAMP NULL COMMENT '만료 시간',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '등록 시간',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시간',
    
    -- 외래키
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- 인덱스
    UNIQUE KEY uk_user_exchange (user_id, exchange_type) COMMENT '사용자별 거래소당 하나의 API 키',
    INDEX idx_user_id (user_id),
    INDEX idx_exchange_type (exchange_type),
    INDEX idx_is_active (is_active),
    INDEX idx_last_used_at (last_used_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='외부 API 키 관리 테이블';
```

### **6. 포트폴리오 스냅샷 테이블 (portfolio_snapshots)**

```sql
CREATE TABLE portfolio_snapshots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '스냅샷 고유 ID',
    user_id BIGINT NOT NULL COMMENT '사용자 ID (FK)',
    snapshot_date DATE NOT NULL COMMENT '스냅샷 날짜',
    total_investment DECIMAL(20,2) NOT NULL DEFAULT 0 COMMENT '총 투자금액',
    total_current_value DECIMAL(20,2) NOT NULL DEFAULT 0 COMMENT '총 현재가치',
    total_profit_loss DECIMAL(20,2) NOT NULL DEFAULT 0 COMMENT '총 손익',
    profit_rate DECIMAL(8,4) NOT NULL DEFAULT 0 COMMENT '수익률 (%)',
    asset_count INT NOT NULL DEFAULT 0 COMMENT '보유 자산 수',
    crypto_value DECIMAL(20,2) DEFAULT 0 COMMENT '암호화폐 가치',
    stock_value DECIMAL(20,2) DEFAULT 0 COMMENT '주식 가치',
    notes TEXT NULL COMMENT '메모',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시간',
    
    -- 외래키
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- 인덱스
    UNIQUE KEY uk_user_date (user_id, snapshot_date) COMMENT '사용자별 일별 스냅샷 중복 방지',
    INDEX idx_user_id (user_id),
    INDEX idx_snapshot_date (snapshot_date),
    INDEX idx_user_snapshot_date (user_id, snapshot_date),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='포트폴리오 일별 스냅샷 테이블';
```

---

## 📈 **성능 최적화 고려사항**

### **인덱스 전략**
1. **복합 인덱스**: 자주 함께 조회되는 컬럼들
2. **커버링 인덱스**: SELECT에 필요한 모든 컬럼 포함
3. **파티셔닝**: price_history 테이블의 날짜별 파티션

### **쿼리 최적화**
1. **페이징 처리**: LIMIT/OFFSET 대신 커서 기반 페이징
2. **집계 쿼리**: 포트폴리오 계산을 위한 효율적인 SUM/GROUP BY
3. **캐싱 전략**: Redis를 활용한 자주 조회되는 데이터 캐싱

---

## 🔒 **보안 고려사항**

### **데이터 암호화**
1. **API 키**: AES-256 암호화 저장
2. **비밀번호**: BCrypt 해싱
3. **민감 정보**: 필요시 컬럼 레벨 암호화

### **접근 제어**
1. **행 레벨 보안**: 사용자별 데이터 격리
2. **외래키 제약**: 데이터 무결성 보장
3. **소프트 삭제**: is_active 컬럼 활용

---

*데이터베이스 스키마 설계 완료: 2025-06-22*
*총 6개 테이블, 성능 최적화 및 보안 고려*
