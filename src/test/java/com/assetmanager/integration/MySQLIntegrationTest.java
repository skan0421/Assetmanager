package com.assetmanager.integration;

import com.assetmanager.domain.*;
import com.assetmanager.mapper.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MySQL 데이터베이스 통합 테스트
 * 실제 MySQL 환경에서 모든 Mapper의 기본 동작을 검증합니다.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@ActiveProfiles("mysql")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MySQLIntegrationTest {

    @Autowired private UserMapper userMapper;
    @Autowired private AssetMapper assetMapper;
    @Autowired private TransactionMapper transactionMapper;
    @Autowired private PriceHistoryMapper priceHistoryMapper;
    @Autowired private ApiKeyMapper apiKeyMapper;
    @Autowired private PortfolioSnapshotMapper portfolioSnapshotMapper;

    private User testUser;
    private Asset testAsset;
    private Long savedUserId;
    private Long savedAssetId;

    @BeforeAll
    void setUp() {
        // 이전 테스트 실행으로 남아있을 수 있는 데이터를 확인하여 중복 삽입을 방지한다.
        Optional<User> existingUserOpt = userMapper.findByEmail("test@mysql.com");
        if (existingUserOpt.isPresent()) {
            testUser = existingUserOpt.get();
        } else {
            testUser = User.builder()
                    .email("test@mysql.com")
                    .password("password123")
                    .name("MySQL Test User")
                    .authProvider(AuthProvider.LOCAL)
                    .role(Role.USER)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            userMapper.insert(testUser);
        }
        savedUserId = testUser.getId();

        Optional<Asset> existingAssetOpt = assetMapper.findByUserIdAndSymbol(savedUserId, "BTC");
        if (existingAssetOpt.isPresent()) {
            testAsset = existingAssetOpt.get();
        } else {
            testAsset = Asset.builder()
                    .userId(savedUserId)
                    .symbol("BTC")
                    .name("Bitcoin")
                    .assetType(AssetType.CRYPTO)
                    .exchange("UPBIT")
                    .quantity(BigDecimal.valueOf(0.5))
                    .averagePrice(BigDecimal.valueOf(50000.00))
                    .currency("KRW")
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            assetMapper.insert(testAsset);
        }
        savedAssetId = testAsset.getId();
    }

    @Test
    @Order(1)
    @DisplayName("MySQL 연결 및 기본 Mapper 동작 테스트")
    void testMySQLConnection() {
        // Given: 테스트 사용자 생성
        String uniqueEmail = "test" + System.currentTimeMillis() + "@mysql.com";
        testUser = User.builder()
                .email(uniqueEmail)
                .password("password123")
                .name("MySQL Test User")
                .authProvider(AuthProvider.LOCAL)
                .role(Role.USER)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // When: 사용자 저장
        userMapper.insert(testUser);
        savedUserId = testUser.getId();

        // Then: 정상 저장 확인
        assertThat(savedUserId).isNotNull();
        
        // 저장된 사용자 조회 확인
        Optional<User> foundUserOpt = userMapper.findById(savedUserId);
        assertThat(foundUserOpt).isPresent();
        
        User foundUser = foundUserOpt.get();
        assertThat(foundUser.getEmail()).isEqualTo(uniqueEmail);
        assertThat(foundUser.getName()).isEqualTo("MySQL Test User");
        
        System.out.println("✅ MySQL 연결 및 UserMapper 동작 확인 완료");
    }

    @Test
    @Order(2)
    @DisplayName("AssetMapper MySQL 테스트")
    void testAssetMapperWithMySQL() {
        // Given: 테스트 자산 생성
        testAsset = Asset.builder()
                .userId(savedUserId)
                .symbol("BTC")
                .name("Bitcoin")
                .assetType(AssetType.CRYPTO)
                .exchange("UPBIT")
                .quantity(BigDecimal.valueOf(0.5))
                .averagePrice(BigDecimal.valueOf(50000.00))
                .currency("KRW")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // When: 자산 저장
        assetMapper.insert(testAsset);
        savedAssetId = testAsset.getId();

        // Then: 정상 저장 확인
        assertThat(savedAssetId).isNotNull();
        
        // 저장된 자산 조회 확인
        Optional<Asset> foundAssetOpt = assetMapper.findById(savedAssetId);
        assertThat(foundAssetOpt).isPresent();
        
        Asset foundAsset = foundAssetOpt.get();
        assertThat(foundAsset.getSymbol()).isEqualTo("BTC");
        assertThat(foundAsset.getUserId()).isEqualTo(savedUserId);
        
        // 사용자별 자산 목록 조회
        List<Asset> userAssets = assetMapper.findByUserId(savedUserId);
        assertThat(userAssets).hasSize(1);
        assertThat(userAssets.get(0).getSymbol()).isEqualTo("BTC");
        
        System.out.println("✅ AssetMapper MySQL 동작 확인 완료");
    }

    @Test
    @Order(3)
    @DisplayName("TransactionMapper MySQL 테스트")
    void testTransactionMapperWithMySQL() {
        // Given: 테스트 거래 생성
        Transaction transaction = Transaction.builder()
                .userId(savedUserId)
                .assetId(savedAssetId)
                .transactionType(TransactionType.BUY)
                .quantity(BigDecimal.valueOf(0.1))
                .price(BigDecimal.valueOf(50000.00))
                .totalAmount(BigDecimal.valueOf(5000.00))
                .fee(BigDecimal.valueOf(25.00))
                .netAmount(BigDecimal.valueOf(4975.00))
                .transactionDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // When: 거래 저장
        transactionMapper.insert(transaction);
        Long transactionId = transaction.getId();

        // Then: 정상 저장 확인
        assertThat(transactionId).isNotNull();
        
        // 저장된 거래 조회 확인
        Optional<Transaction> foundTransactionOpt = transactionMapper.findById(transactionId);
        assertThat(foundTransactionOpt).isPresent();
        
        Transaction foundTransaction = foundTransactionOpt.get();
        assertThat(foundTransaction.getTransactionType()).isEqualTo(TransactionType.BUY);
        
        // 사용자별 거래 목록 조회
        List<Transaction> userTransactions = transactionMapper.findByUserId(savedUserId);
        assertThat(userTransactions).hasSize(1);
        
        System.out.println("✅ TransactionMapper MySQL 동작 확인 완료");
    }

    @Test
    @Order(4)
    @DisplayName("PriceHistoryMapper MySQL 테스트")
    void testPriceHistoryMapperWithMySQL() {
        // Given: 테스트 가격 이력 생성
        PriceHistory priceHistory = PriceHistory.builder()
                .symbol("BTC")
                .exchange("UPBIT")
                .price(BigDecimal.valueOf(51000.00))
                .volume(BigDecimal.valueOf(1000.0))
                .dataSource("UPBIT")
                .priceTimestamp(LocalDateTime.now().plusNanos(System.nanoTime() % 1_000_000))
                .createdAt(LocalDateTime.now())
                .build();

        // When: 가격 이력 저장
        priceHistoryMapper.insert(priceHistory);
        Long priceHistoryId = priceHistory.getId();

        // Then: 정상 저장 확인
        assertThat(priceHistoryId).isNotNull();
        
        // 저장된 가격 이력 조회 확인
        Optional<PriceHistory> foundPriceHistoryOpt = priceHistoryMapper.findById(priceHistoryId);
        assertThat(foundPriceHistoryOpt).isPresent();
        
        PriceHistory foundPriceHistory = foundPriceHistoryOpt.get();
        assertThat(foundPriceHistory.getSymbol()).isEqualTo("BTC");
        assertThat(foundPriceHistory.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(51000.00));
        
        // 심볼별 최신 가격 조회
        Optional<PriceHistory> latestPriceOpt = priceHistoryMapper.findLatestBySymbol("BTC");
        assertThat(latestPriceOpt).isPresent();
        
        PriceHistory latestPrice = latestPriceOpt.get();
        assertThat(latestPrice.getSymbol()).isEqualTo("BTC");
        
        System.out.println("✅ PriceHistoryMapper MySQL 동작 확인 완료");
    }

    @Test
    @Order(5)
    @DisplayName("ApiKeyMapper MySQL 테스트")
    void testApiKeyMapperWithMySQL() {
        // Given: 테스트 API 키 생성
        ApiKey apiKey = ApiKey.builder()
                .userId(savedUserId)
                .exchangeType(ExchangeType.UPBIT)
                .exchangeName("UPBIT")
                .accessKey("test-access-key")
                .secretKey("test-secret-key")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // When: API 키 저장
        apiKeyMapper.insert(apiKey);
        Long apiKeyId = apiKey.getId();

        // Then: 정상 저장 확인
        assertThat(apiKeyId).isNotNull();
        
        // 저장된 API 키 조회 확인
        Optional<ApiKey> foundApiKeyOpt = apiKeyMapper.findById(apiKeyId);
        assertThat(foundApiKeyOpt).isPresent();
        
        ApiKey foundApiKey = foundApiKeyOpt.get();
        assertThat(foundApiKey.getExchangeType()).isEqualTo(ExchangeType.UPBIT);
        assertThat(foundApiKey.getAccessKey()).isEqualTo("test-access-key");
        
        // 사용자별 API 키 목록 조회
        List<ApiKey> userApiKeys = apiKeyMapper.findByUserId(savedUserId);
        assertThat(userApiKeys).hasSize(1);
        assertThat(userApiKeys.get(0).getExchangeType()).isEqualTo(ExchangeType.UPBIT);
        
        System.out.println("✅ ApiKeyMapper MySQL 동작 확인 완료");
    }

    @Test
    @Order(6)
    @DisplayName("PortfolioSnapshotMapper MySQL 테스트")
    void testPortfolioSnapshotMapperWithMySQL() {
        // Given: 테스트 포트폴리오 스냅샷 생성
        PortfolioSnapshot snapshot = PortfolioSnapshot.builder()
                .userId(savedUserId)
                .snapshotDate(LocalDate.now())
                .totalInvestment(BigDecimal.valueOf(95000.00))
                .totalCurrentValue(BigDecimal.valueOf(100000.00))
                .totalProfitLoss(BigDecimal.valueOf(5000.00))
                .profitRate(BigDecimal.valueOf(5.26))
                .assetCount(1)
                .cryptoValue(BigDecimal.valueOf(100000.00))
                .stockValue(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .build();

        // When: 포트폴리오 스냅샷 저장
        portfolioSnapshotMapper.insert(snapshot);
        Long snapshotId = snapshot.getId();

        // Then: 정상 저장 확인
        assertThat(snapshotId).isNotNull();
        
        // 저장된 스냅샷 조회 확인
        Optional<PortfolioSnapshot> foundSnapshotOpt = portfolioSnapshotMapper.findById(snapshotId);
        assertThat(foundSnapshotOpt).isPresent();
        
        PortfolioSnapshot foundSnapshot = foundSnapshotOpt.get();
        assertThat(foundSnapshot.getTotalCurrentValue()).isEqualByComparingTo(BigDecimal.valueOf(100000.00));
        assertThat(foundSnapshot.getUserId()).isEqualTo(savedUserId);
        
        // 사용자별 스냅샷 목록 조회
        List<PortfolioSnapshot> userSnapshots = portfolioSnapshotMapper.findByUserId(savedUserId);
        assertThat(userSnapshots).hasSize(1);
        
        System.out.println("✅ PortfolioSnapshotMapper MySQL 동작 확인 완료");
    }

    @Test
    @Order(7)
    @DisplayName("전체 MySQL 통합 테스트 요약")
    void testMySQLIntegrationSummary() {
        // 전체 데이터 확인
        List<User> allUsers = userMapper.findAll();
        List<Asset> userAssets = assetMapper.findByUserId(savedUserId);
        List<Transaction> userTransactions = transactionMapper.findByUserId(savedUserId);
        List<PriceHistory> btcPriceHistory = priceHistoryMapper.findBySymbolWithLimit("BTC", 10);
        List<ApiKey> userApiKeys = apiKeyMapper.findByUserId(savedUserId);
        List<PortfolioSnapshot> userSnapshots = portfolioSnapshotMapper.findByUserId(savedUserId);

        System.out.println("\n=== MySQL 통합 테스트 결과 요약 ===");
        System.out.println("👤 전체 사용자 수: " + allUsers.size());
        System.out.println("💰 테스트 사용자 자산 수: " + userAssets.size());
        System.out.println("📊 테스트 사용자 거래 수: " + userTransactions.size());
        System.out.println("📈 BTC 가격 이력 수: " + btcPriceHistory.size());
        System.out.println("🔑 테스트 사용자 API 키 수: " + userApiKeys.size());
        System.out.println("📸 테스트 사용자 포트폴리오 스냅샷 수: " + userSnapshots.size());
        System.out.println("========================================");
        
        // 모든 Mapper가 정상 동작 확인
        assertThat(allUsers).isNotEmpty();
        assertThat(userAssets).isNotEmpty();
        assertThat(userTransactions).isNotEmpty();
        assertThat(btcPriceHistory).isNotEmpty();
        assertThat(userApiKeys).isNotEmpty();
        assertThat(userSnapshots).isNotEmpty();
        
        System.out.println("✅ 모든 Mapper가 MySQL에서 정상 동작 확인 완료!");
    }
    
    @Test
    @Order(8)
    @DisplayName("MySQL 기본 쿼리 테스트")
    void testMySQLBasicQueries() {
        // 활성 사용자 수 확인
        int activeUserCount = userMapper.countActiveUsers();
        assertThat(activeUserCount).isGreaterThan(0);
        
        // 거래소별 가격 정보 조회
        Optional<PriceHistory> upbitBtcPrice = priceHistoryMapper.findLatestBySymbolAndExchange("BTC", "UPBIT");
        assertThat(upbitBtcPrice).isPresent();
        
        System.out.println("✅ MySQL 기본 쿼리 테스트 완료");
        
        // 성능 지표 출력
        System.out.println("\n=== 성능 지표 ===");
        System.out.println("💡 활성 사용자 수: " + activeUserCount);
        System.out.println("================");
    }
    
    @Test
    @Order(9)
    @DisplayName("데이터 무결성 테스트")
    void testDataIntegrity() {
        // 사용자 이메일 중복 확인
        int emailCount = userMapper.countByEmail("test@mysql.com");
        assertThat(emailCount).isEqualTo(1);
        
        // 자산의 사용자 FK 확인
        List<Asset> userAssets = assetMapper.findByUserId(savedUserId);
        for (Asset asset : userAssets) {
            assertThat(asset.getUserId()).isEqualTo(savedUserId);
        }
        
        // 거래의 자산 FK 확인
        List<Transaction> userTransactions = transactionMapper.findByUserId(savedUserId);
        for (Transaction transaction : userTransactions) {
            assertThat(transaction.getUserId()).isEqualTo(savedUserId);
            assertThat(transaction.getAssetId()).isEqualTo(savedAssetId);
        }
        
        // API 키의 사용자 FK 확인
        List<ApiKey> userApiKeys = apiKeyMapper.findByUserId(savedUserId);
        for (ApiKey apiKey : userApiKeys) {
            assertThat(apiKey.getUserId()).isEqualTo(savedUserId);
        }
        
        System.out.println("✅ 데이터 무결성 테스트 완료");
    }
}
