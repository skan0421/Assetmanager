package com.assetmanager.mapper;

import com.assetmanager.domain.Asset;
import com.assetmanager.domain.AssetType;
import com.assetmanager.domain.User;
import com.assetmanager.domain.AuthProvider;
import com.assetmanager.domain.Role;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * AssetMapper 테스트 클래스 (database-schema.md 기반)
 * Phase 2.3: MyBatis Mapper 구현 - AssetMapper 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AssetMapperTest {

    @Autowired
    private AssetMapper assetMapper;
    
    @Autowired
    private UserMapper userMapper;

    private Asset testAsset;
    private Long testUserId;
    private User testUser;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = User.builder()
                .email("asset.test@example.com")
                .password("encoded_password123")
                .name("Asset Test User")
                .authProvider(AuthProvider.LOCAL)
                .role(Role.USER)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        userMapper.insert(testUser);
        testUserId = testUser.getId();
        
        testAsset = Asset.builder()
                .userId(testUserId)
                .symbol("BTC")
                .name("Bitcoin")
                .assetType(AssetType.CRYPTO)
                .exchange("UPBIT")
                .countryCode("KR")
                .quantity(new BigDecimal("0.5"))
                .averagePrice(new BigDecimal("50000000"))
                .currency("KRW")
                .isActive(true)
                .notes("Test Bitcoin asset")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // =================
    // 기본 CRUD 테스트
    // =================

    @Test
    @Order(1)
    @DisplayName("자산 등록 테스트")
    void testInsert() {
        // When
        assetMapper.insert(testAsset);

        // Then
        assertThat(testAsset.getId()).isNotNull();
        assertThat(testAsset.getId()).isGreaterThan(0L);
    }

    @Test
    @Order(2)
    @DisplayName("ID로 자산 조회 테스트")
    void testFindById() {
        // Given
        assetMapper.insert(testAsset);
        Long assetId = testAsset.getId();

        // When
        Optional<Asset> found = assetMapper.findById(assetId);

        // Then
        assertThat(found).isPresent();
        Asset asset = found.get();
        assertThat(asset.getSymbol()).isEqualTo("BTC");
        assertThat(asset.getName()).isEqualTo("Bitcoin");
        assertThat(asset.getAssetType()).isEqualTo(AssetType.CRYPTO);
        assertThat(asset.getExchange()).isEqualTo("UPBIT");
        assertThat(asset.getQuantity()).isEqualByComparingTo(new BigDecimal("0.5"));
        assertThat(asset.getAveragePrice()).isEqualByComparingTo(new BigDecimal("50000000"));
    }

    @Test
    @Order(3)
    @DisplayName("자산 정보 수정 테스트")
    void testUpdate() {
        // Given
        assetMapper.insert(testAsset);
        Long assetId = testAsset.getId();

        // When
        testAsset.setQuantity(new BigDecimal("1.0"));
        testAsset.setAveragePrice(new BigDecimal("55000000"));
        testAsset.setNotes("Updated Bitcoin asset");
        assetMapper.update(testAsset);

        // Then
        Optional<Asset> updated = assetMapper.findById(assetId);
        assertThat(updated).isPresent();
        Asset asset = updated.get();
        assertThat(asset.getQuantity()).isEqualByComparingTo(new BigDecimal("1.0"));
        assertThat(asset.getAveragePrice()).isEqualByComparingTo(new BigDecimal("55000000"));
        assertThat(asset.getNotes()).isEqualTo("Updated Bitcoin asset");
    }

    @Test
    @Order(4)
    @DisplayName("자산 소프트 삭제 테스트")
    void testSoftDelete() {
        // Given
        assetMapper.insert(testAsset);
        Long assetId = testAsset.getId();

        // When
        assetMapper.softDelete(assetId);

        // Then
        Optional<Asset> deleted = assetMapper.findById(assetId);
        assertThat(deleted).isPresent();
        assertThat(deleted.get().getIsActive()).isFalse();
    }

    // =================
    // 사용자별 자산 조회 테스트
    // =================

    @Test
    @Order(5)
    @DisplayName("사용자의 모든 자산 조회 테스트")
    void testFindByUserId() {
        // Given
        assetMapper.insert(testAsset);
        
        Asset ethAsset = Asset.builder()
                .userId(testUserId)
                .symbol("ETH")
                .name("Ethereum")
                .assetType(AssetType.CRYPTO)
                .exchange("UPBIT")
                .countryCode("KR")
                .quantity(new BigDecimal("2.0"))
                .averagePrice(new BigDecimal("3000000"))
                .currency("KRW")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        assetMapper.insert(ethAsset);

        // When
        List<Asset> assets = assetMapper.findByUserId(testUserId);

        // Then
        assertThat(assets).hasSize(2);
        assertThat(assets).extracting(Asset::getSymbol)
                .containsExactlyInAnyOrder("BTC", "ETH");
    }

    @Test
    @Order(6)
    @DisplayName("사용자의 활성 자산만 조회 테스트")
    void testFindActiveAssetsByUserId() {
        // Given
        assetMapper.insert(testAsset);
        
        Asset inactiveAsset = Asset.builder()
                .userId(testUserId)
                .symbol("ADA")
                .name("Cardano")
                .assetType(AssetType.CRYPTO)
                .exchange("UPBIT")
                .countryCode("KR")
                .quantity(new BigDecimal("100"))
                .averagePrice(new BigDecimal("1000"))
                .currency("KRW")
                .isActive(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        assetMapper.insert(inactiveAsset);

        // When
        List<Asset> activeAssets = assetMapper.findActiveAssetsByUserId(testUserId);

        // Then
        assertThat(activeAssets).hasSize(1);
        assertThat(activeAssets.get(0).getSymbol()).isEqualTo("BTC");
        assertThat(activeAssets.get(0).getIsActive()).isTrue();
    }

    @Test
    @Order(7)
    @DisplayName("자산 타입별 조회 테스트")
    void testFindByUserIdAndAssetType() {
        // Given
        assetMapper.insert(testAsset); // CRYPTO
        
        Asset stockAsset = Asset.builder()
                .userId(testUserId)
                .symbol("AAPL")
                .name("Apple Inc.")
                .assetType(AssetType.STOCK)
                .exchange("NASDAQ")
                .countryCode("US")
                .quantity(new BigDecimal("10"))
                .averagePrice(new BigDecimal("150"))
                .currency("USD")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        assetMapper.insert(stockAsset);

        // When
        List<Asset> cryptoAssets = assetMapper.findByUserIdAndAssetType(testUserId, AssetType.CRYPTO);
        List<Asset> stockAssets = assetMapper.findByUserIdAndAssetType(testUserId, AssetType.STOCK);

        // Then
        assertThat(cryptoAssets).hasSize(1);
        assertThat(cryptoAssets.get(0).getSymbol()).isEqualTo("BTC");
        
        assertThat(stockAssets).hasSize(1);
        assertThat(stockAssets.get(0).getSymbol()).isEqualTo("AAPL");
    }

    @Test
    @Order(8)
    @DisplayName("심볼로 자산 조회 테스트")
    void testFindByUserIdAndSymbol() {
        // Given
        assetMapper.insert(testAsset);

        // When
        Optional<Asset> found = assetMapper.findByUserIdAndSymbol(testUserId, "BTC");
        Optional<Asset> notFound = assetMapper.findByUserIdAndSymbol(testUserId, "UNKNOWN");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Bitcoin");
        
        assertThat(notFound).isEmpty();
    }

    // =================
    // 포트폴리오 계산용 쿼리 테스트
    // =================

    @Test
    @Order(9)
    @DisplayName("총 투자 금액 계산 테스트")
    void testGetTotalInvestmentByUserId() {
        // Given
        assetMapper.insert(testAsset); // 0.5 * 50,000,000 = 25,000,000
        
        Asset ethAsset = Asset.builder()
                .userId(testUserId)
                .symbol("ETH")
                .name("Ethereum")
                .assetType(AssetType.CRYPTO)
                .exchange("UPBIT")
                .countryCode("KR")
                .quantity(new BigDecimal("2.0"))
                .averagePrice(new BigDecimal("3000000"))
                .currency("KRW")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        assetMapper.insert(ethAsset); // 2.0 * 3,000,000 = 6,000,000

        // When
        BigDecimal totalInvestment = assetMapper.getTotalInvestmentByUserId(testUserId);

        // Then
        // 25,000,000 + 6,000,000 = 31,000,000
        assertThat(totalInvestment).isEqualByComparingTo(new BigDecimal("31000000"));
    }

    @Test
    @Order(10)
    @DisplayName("활성 자산 개수 조회 테스트")
    void testCountActiveAssetsByUserId() {
        // Given
        assetMapper.insert(testAsset);
        
        Asset ethAsset = Asset.builder()
                .userId(testUserId)
                .symbol("ETH")
                .name("Ethereum")
                .assetType(AssetType.CRYPTO)
                .exchange("UPBIT")
                .countryCode("KR")
                .quantity(new BigDecimal("2.0"))
                .averagePrice(new BigDecimal("3000000"))
                .currency("KRW")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        assetMapper.insert(ethAsset);

        Asset inactiveAsset = Asset.builder()
                .userId(testUserId)
                .symbol("ADA")
                .name("Cardano")
                .assetType(AssetType.CRYPTO)
                .exchange("UPBIT")
                .countryCode("KR")
                .quantity(new BigDecimal("100"))
                .averagePrice(new BigDecimal("1000"))
                .currency("KRW")
                .isActive(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        assetMapper.insert(inactiveAsset);

        // When
        int activeCount = assetMapper.countActiveAssetsByUserId(testUserId);

        // Then
        assertThat(activeCount).isEqualTo(2);
    }

    @Test
    @Order(11)
    @DisplayName("자산 타입별 투자 금액 계산 테스트")
    void testGetInvestmentByAssetType() {
        // Given
        assetMapper.insert(testAsset); // CRYPTO: 25,000,000
        
        Asset stockAsset = Asset.builder()
                .userId(testUserId)
                .symbol("AAPL")
                .name("Apple Inc.")
                .assetType(AssetType.STOCK)
                .exchange("NASDAQ")
                .countryCode("US")
                .quantity(new BigDecimal("10"))
                .averagePrice(new BigDecimal("150"))
                .currency("USD")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        assetMapper.insert(stockAsset); // STOCK: 1,500

        // When
        BigDecimal cryptoInvestment = assetMapper.getInvestmentByAssetType(testUserId, AssetType.CRYPTO);
        BigDecimal stockInvestment = assetMapper.getInvestmentByAssetType(testUserId, AssetType.STOCK);

        // Then
        assertThat(cryptoInvestment).isEqualByComparingTo(new BigDecimal("25000000"));
        assertThat(stockInvestment).isEqualByComparingTo(new BigDecimal("1500"));
    }

    @Test
    @Order(12)
    @DisplayName("가장 많이 투자한 자산 Top N 테스트")
    void testFindTopInvestmentAssets() {
        // Given
        assetMapper.insert(testAsset); // BTC: 25,000,000
        
        Asset ethAsset = Asset.builder()
                .userId(testUserId)
                .symbol("ETH")
                .name("Ethereum")
                .assetType(AssetType.CRYPTO)
                .exchange("UPBIT")
                .countryCode("KR")
                .quantity(new BigDecimal("10"))
                .averagePrice(new BigDecimal("3000000"))
                .currency("KRW")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        assetMapper.insert(ethAsset); // ETH: 30,000,000

        Asset adaAsset = Asset.builder()
                .userId(testUserId)
                .symbol("ADA")
                .name("Cardano")
                .assetType(AssetType.CRYPTO)
                .exchange("UPBIT")
                .countryCode("KR")
                .quantity(new BigDecimal("1000"))
                .averagePrice(new BigDecimal("1000"))
                .currency("KRW")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        assetMapper.insert(adaAsset); // ADA: 1,000,000

        // When
        List<Asset> topAssets = assetMapper.findTopInvestmentAssets(testUserId, 2);

        // Then
        assertThat(topAssets).hasSize(2);
        assertThat(topAssets.get(0).getSymbol()).isEqualTo("ETH"); // 가장 큰 투자 금액
        assertThat(topAssets.get(1).getSymbol()).isEqualTo("BTC"); // 두 번째 큰 투자 금액
    }

    // =================
    // 비즈니스 로직용 쿼리 테스트
    // =================

    @Test
    @Order(13)
    @DisplayName("보유 수량이 있는 자산만 조회 테스트")
    void testFindHoldingAssetsByUserId() {
        // Given
        assetMapper.insert(testAsset); // quantity: 0.5 > 0
        
        Asset zeroAsset = Asset.builder()
                .userId(testUserId)
                .symbol("ETH")
                .name("Ethereum")
                .assetType(AssetType.CRYPTO)
                .exchange("UPBIT")
                .countryCode("KR")
                .quantity(BigDecimal.ZERO)
                .averagePrice(new BigDecimal("3000000"))
                .currency("KRW")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        assetMapper.insert(zeroAsset); // quantity: 0

        // When
        List<Asset> holdingAssets = assetMapper.findHoldingAssetsByUserId(testUserId);

        // Then
        assertThat(holdingAssets).hasSize(1);
        assertThat(holdingAssets.get(0).getSymbol()).isEqualTo("BTC");
        assertThat(holdingAssets.get(0).getQuantity()).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    @Order(14)
    @DisplayName("거래소별 자산 조회 테스트")
    void testFindByUserIdAndExchange() {
        // Given
        assetMapper.insert(testAsset); // UPBIT
        
        Asset binanceAsset = Asset.builder()
                .userId(testUserId)
                .symbol("ETH")
                .name("Ethereum")
                .assetType(AssetType.CRYPTO)
                .exchange("BINANCE")
                .countryCode("KR")
                .quantity(new BigDecimal("1"))
                .averagePrice(new BigDecimal("3000000"))
                .currency("KRW")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        assetMapper.insert(binanceAsset);

        // When
        List<Asset> upbitAssets = assetMapper.findByUserIdAndExchange(testUserId, "UPBIT");
        List<Asset> binanceAssets = assetMapper.findByUserIdAndExchange(testUserId, "BINANCE");

        // Then
        assertThat(upbitAssets).hasSize(1);
        assertThat(upbitAssets.get(0).getSymbol()).isEqualTo("BTC");
        
        assertThat(binanceAssets).hasSize(1);
        assertThat(binanceAssets.get(0).getSymbol()).isEqualTo("ETH");
    }

    @Test
    @Order(15)
    @DisplayName("국가 코드별 자산 조회 테스트")
    void testFindByUserIdAndCountryCode() {
        // Given
        assetMapper.insert(testAsset); // KR
        
        Asset usAsset = Asset.builder()
                .userId(testUserId)
                .symbol("AAPL")
                .name("Apple Inc.")
                .assetType(AssetType.STOCK)
                .exchange("NASDAQ")
                .countryCode("US")
                .quantity(new BigDecimal("10"))
                .averagePrice(new BigDecimal("150"))
                .currency("USD")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        assetMapper.insert(usAsset);

        // When
        List<Asset> krAssets = assetMapper.findByUserIdAndCountryCode(testUserId, "KR");
        List<Asset> usAssets = assetMapper.findByUserIdAndCountryCode(testUserId, "US");

        // Then
        assertThat(krAssets).hasSize(1);
        assertThat(krAssets.get(0).getSymbol()).isEqualTo("BTC");
        
        assertThat(usAssets).hasSize(1);
        assertThat(usAssets.get(0).getSymbol()).isEqualTo("AAPL");
    }

    @Test
    @Order(16)
    @DisplayName("통화별 자산 조회 테스트")
    void testFindByUserIdAndCurrency() {
        // Given
        assetMapper.insert(testAsset); // KRW
        
        Asset usdAsset = Asset.builder()
                .userId(testUserId)
                .symbol("AAPL")
                .name("Apple Inc.")
                .assetType(AssetType.STOCK)
                .exchange("NASDAQ")
                .countryCode("US")
                .quantity(new BigDecimal("10"))
                .averagePrice(new BigDecimal("150"))
                .currency("USD")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        assetMapper.insert(usdAsset);

        // When
        List<Asset> krwAssets = assetMapper.findByUserIdAndCurrency(testUserId, "KRW");
        List<Asset> usdAssets = assetMapper.findByUserIdAndCurrency(testUserId, "USD");

        // Then
        assertThat(krwAssets).hasSize(1);
        assertThat(krwAssets.get(0).getSymbol()).isEqualTo("BTC");
        
        assertThat(usdAssets).hasSize(1);
        assertThat(usdAssets.get(0).getSymbol()).isEqualTo("AAPL");
    }

    @Test
    @Order(17)
    @DisplayName("자산 타입별 개수 조회 테스트")
    void testCountAssetsByType() {
        // Given
        assetMapper.insert(testAsset); // CRYPTO
        
        Asset ethAsset = Asset.builder()
                .userId(testUserId)
                .symbol("ETH")
                .name("Ethereum")
                .assetType(AssetType.CRYPTO)
                .exchange("UPBIT")
                .countryCode("KR")
                .quantity(new BigDecimal("2"))
                .averagePrice(new BigDecimal("3000000"))
                .currency("KRW")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        assetMapper.insert(ethAsset); // CRYPTO

        Asset stockAsset = Asset.builder()
                .userId(testUserId)
                .symbol("AAPL")
                .name("Apple Inc.")
                .assetType(AssetType.STOCK)
                .exchange("NASDAQ")
                .countryCode("US")
                .quantity(new BigDecimal("10"))
                .averagePrice(new BigDecimal("150"))
                .currency("USD")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        assetMapper.insert(stockAsset); // STOCK

        // When
        int cryptoCount = assetMapper.countAssetsByType(testUserId, AssetType.CRYPTO);
        int stockCount = assetMapper.countAssetsByType(testUserId, AssetType.STOCK);

        // Then
        assertThat(cryptoCount).isEqualTo(2);
        assertThat(stockCount).isEqualTo(1);
    }

    @Test
    @Order(18)
    @DisplayName("빈 결과 조회 테스트")
    void testEmptyResults() {
        // When
        List<Asset> assets = assetMapper.findByUserId(999L); // 존재하지 않는 사용자
        BigDecimal investment = assetMapper.getTotalInvestmentByUserId(999L);
        int count = assetMapper.countActiveAssetsByUserId(999L);

        // Then
        assertThat(assets).isEmpty();
        assertThat(investment).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(count).isEqualTo(0);
    }
}
