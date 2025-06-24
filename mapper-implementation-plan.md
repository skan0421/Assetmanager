# 🗺️ MyBatis Mapper 구현 계획서

## 📋 **프로젝트 현황**
- **현재 Phase**: 2.3 MyBatis Mapper 구현
- **완료된 Domain 모델**: User, Asset, Transaction, PriceHistory, ApiKey, PortfolioSnapshot
- **현재 Mapper 상태**: UserMapper 기본 틀만 존재 (재구현 필요)

---

## 🎯 **구현해야 할 Mapper 목록**

### **1. UserMapper** 🔄 (재구현 필요)
**파일**: `src/main/java/com/assetmanager/mapper/UserMapper.java`

**핵심 기능:**
- **기본 CRUD**: insert, findById, findByEmail, update, softDelete
- **인증 관련**: updateLastLogin, findByAuthProvider
- **관리 기능**: findActiveUsers, countActiveUsers
- **소셜 로그인**: 소셜 로그인 사용자 관리

**비즈니스 쿼리:**
- 활성 사용자 통계
- 소셜 로그인 제공자별 사용자 조회
- 역할별 사용자 관리

### **2. AssetMapper** ✨ (신규 구현)
**파일**: `src/main/java/com/assetmanager/mapper/AssetMapper.java`

**핵심 기능:**
- **기본 CRUD**: insert, findById, update, softDelete
- **사용자별 조회**: findByUserId, findActiveAssetsByUserId
- **자산 타입별**: findByUserIdAndAssetType (CRYPTO/STOCK)
- **거래소별**: findByUserIdAndExchange

**포트폴리오 계산 쿼리:**
- getTotalInvestmentByUserId: 총 투자 금액
- countActiveAssetsByUserId: 활성 자산 개수
- getInvestmentByAssetType: 자산 타입별 투자 금액
- findTopInvestmentAssets: 투자 금액 상위 자산

**비즈니스 쿼리:**
- findHoldingAssetsByUserId: 보유 수량이 있는 자산
- 심볼별 자산 조회
- 평균 매수가 계산

### **3. TransactionMapper** ✨ (신규 구현)
**파일**: `src/main/java/com/assetmanager/mapper/TransactionMapper.java`

**핵심 기능:**
- **기본 CRUD**: insert, findById, update, delete
- **사용자별 조회**: findByUserId, findByUserIdWithPaging
- **자산별 조회**: findByUserIdAndAssetId
- **거래 타입별**: findByUserIdAndTransactionType (BUY/SELL)

**기간별 조회:**
- findByUserIdAndDateRange: 특정 기간 거래 내역
- findRecentTransactions: 최근 N일 거래 내역
- findRecentTransactionsByUserId: 최근 거래 N개

**통계 및 집계:**
- getTotalBuyAmountByUserId: 총 매수 금액
- getTotalSellAmountByUserId: 총 매도 금액
- getTotalFeeByUserId: 총 거래 수수료
- getAveragePurchasePrice: 특정 자산 평균 매수가
- getMonthlyTransactionStats: 월별 거래 통계

### **4. PriceHistoryMapper** ✨ (신규 구현)
**파일**: `src/main/java/com/assetmanager/mapper/PriceHistoryMapper.java`

**핵심 기능:**
- **기본 CRUD**: insert, findById, update, delete
- **심볼별 조회**: findLatestBySymbol, findBySymbolWithLimit
- **거래소별**: findLatestBySymbolAndExchange
- **기간별**: findBySymbolAndDateRange, findRecentPriceHistory

**가격 분석 쿼리:**
- getMaxPriceInPeriod: 기간별 최고가
- getMinPriceInPeriod: 기간별 최저가
- getAveragePriceInPeriod: 기간별 평균가
- getTotalVolumeInPeriod: 기간별 총 거래량

**데이터 관리:**
- insertBatch: 대량 가격 데이터 삽입
- upsertPriceHistory: 가격 업데이트 또는 삽입
- deleteOldPriceData: 오래된 데이터 정리
- countDuplicateData: 중복 데이터 확인

**실시간 기능:**
- 최신 가격 업데이트
- 거래소별 심볼 목록 조회
- 실시간 가격 배치 처리

### **5. ApiKeyMapper** ✨ (신규 구현)
**파일**: `src/main/java/com/assetmanager/mapper/ApiKeyMapper.java`

**핵심 기능:**
- **기본 CRUD**: insert, findById, update, delete, deactivate
- **사용자별 조회**: findByUserId, findActiveApiKeysByUserId
- **거래소별**: findByUserIdAndExchangeType, findByUserIdAndExchangeName
- **보안 관리**: updateLastUsed, updateActiveStatus

**상태 관리:**
- findExpiredApiKeys: 만료된 API 키 조회
- findApiKeysExpiringBefore: 만료 예정 API 키
- countActiveApiKeysByUserId: 활성 API 키 개수

**거래소 타입별:**
- findCryptoApiKeysByUserId: 암호화폐 거래소 API 키
- findStockApiKeysByUserId: 주식 거래소 API 키
- findAllActiveExchanges: 등록된 거래소 목록

**권한 관리:**
- findTradingApiKeysByUserId: 거래 권한 API 키
- findReadOnlyApiKeysByUserId: 읽기 전용 API 키
- JSON 권한 데이터 처리

**배치 작업:**
- deactivateExpiredApiKeys: 만료 API 키 일괄 비활성화
- deleteOldInactiveApiKeys: 오래된 비활성 키 정리

### **6. PortfolioSnapshotMapper** ✨ (신규 구현)
**파일**: `src/main/java/com/assetmanager/mapper/PortfolioSnapshotMapper.java`

**핵심 기능:**
- **기본 CRUD**: insert, findById, update, delete
- **사용자별 조회**: findByUserId, findByUserIdWithPaging
- **날짜별**: findByUserIdAndDate, findLatestByUserId
- **기간별**: findByUserIdAndDateRange, findRecentSnapshots

**주기별 스냅샷:**
- findMonthlySnapshots: 월별 스냅샷 (매월 마지막 날)
- findWeeklySnapshots: 주별 스냅샷 (매주 마지막 날)
- findDailySnapshots: 일별 스냅샷

**포트폴리오 분석:**
- getMaxProfitRateInPeriod: 기간별 최고 수익률
- getMinProfitRateInPeriod: 기간별 최저 수익률
- getAverageProfitRateInPeriod: 기간별 평균 수익률
- getMaxPortfolioValue: 최대 포트폴리오 가치

**성과 분석:**
- countProfitDays: 수익 발생일 수
- countLossDays: 손실 발생일 수
- getTotalGrowthRate: 총 성장률
- getProfitRateVolatility: 수익률 변동성

**자산 분배 분석:**
- getAverageCryptoWeight: 평균 암호화폐 비중
- getAverageStockWeight: 평균 주식 비중
- 다양성 지수 계산

**배치 작업:**
- insertBatch: 스냅샷 대량 삽입
- upsertSnapshot: 스냅샷 업데이트 또는 삽입
- deleteOldSnapshots: 오래된 스냅샷 정리

---

## 🎯 **구현 우선순위**

### **Phase 2.3.1: 핵심 Mapper (1주차)**
1. **UserMapper** - 인증 시스템 기반
2. **AssetMapper** - 자산 관리 핵심
3. **TransactionMapper** - 거래 내역 관리

### **Phase 2.3.2: 데이터 Mapper (2주차)**
4. **PriceHistoryMapper** - 가격 데이터 관리
5. **ApiKeyMapper** - 외부 API 연동 준비

### **Phase 2.3.3: 분석 Mapper (3주차)**
6. **PortfolioSnapshotMapper** - 포트폴리오 분석

---

## 🔧 **구현 가이드라인**

### **어노테이션 방식 vs XML 방식**
- **간단한 쿼리**: `@Select`, `@Insert`, `@Update`, `@Delete` 어노테이션 사용
- **복잡한 쿼리**: XML 매퍼 파일 사용 (src/main/resources/mapper/)
- **동적 쿼리**: MyBatis `<script>` 태그 또는 XML 방식

### **성능 고려사항**
- **대량 데이터**: `insertBatch`, `@Options(useGeneratedKeys = true)`
- **페이징**: `LIMIT`, `OFFSET` 활용
- **인덱스 활용**: WHERE 절 최적화
- **연관 쿼리**: JOIN vs 분리 쿼리 선택

### **보안 고려사항**
- **파라미터 바인딩**: `#{parameter}` 사용 (SQL Injection 방지)
- **민감한 데이터**: API 키 암호화 처리
- **권한 검증**: 사용자별 데이터 접근 제어

---

## 📋 **다음 단계**
1. UserMapper 재구현 (기본 CRUD + 인증 기능)
2. AssetMapper 구현 (자산 관리 + 포트폴리오 계산)
3. TransactionMapper 구현 (거래 관리 + 통계)
4. 각 Mapper별 단위 테스트 작성
5. Phase 2.4: 데이터베이스 연결 테스트

*Mapper 구현 계획서 - 2025-06-24*
*Phase 2.3: MyBatis Mapper 구현을 위한 상세 계획*
