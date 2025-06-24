package com.assetmanager.mapper;

import com.assetmanager.domain.Asset;
import com.assetmanager.domain.AssetType;
import com.assetmanager.domain.User;
import com.assetmanager.domain.Role;
import com.assetmanager.domain.AuthProvider;
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

    private User testUser;
    private Asset testCryptoAsset;
    private Asset testStockAsset;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = User.builder()
                .email("asset.test@example.com")
                .name("Asset Test User")
                .role(Role.USER)
                .authProvider(AuthProvider.LOCAL)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        userMapper.insert(testUser);

        // 테스트용 암호화폐 자산 생성
        testCryptoAsset = Asset.builder()
                .userId(testUser.getId())
                .symbol("BTC")
                .name("Bitcoin")
                .assetType(AssetType.CRYPTO)
                .exchange("upbit")
                .countryCode("KR")
                .quantity(new BigDecimal("1.5"))
                .averagePrice(new BigDecimal("50000000"))
                .currency("KRW")
                .isActive(true)
                .notes("Test crypto asset")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 테스트용 주식 자산 생성
        testStockAsset = Asset.builder()
                .userId(testUser.getId())
                .symbol("AAPL")
                .name("Apple Inc.")
                .assetType(AssetType.STOCK)
                .exchange("NASDAQ")
                .countryCode("US")
                .quantity(new BigDecimal("10"))
                .averagePrice(new BigDecimal("150.50"))
                .currency("USD")
                .isActive(true)
                .notes("Test stock asset")
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
        assetMapper.insert(testCryptoAsset);

        // Then
        assertThat(testCryptoAsset.getId()).isNotNull();
        assertThat(testCryptoAsset.getId()).isGreaterThan(0L);
    }

    @Test
    @Order(2)
    @DisplayName("ID로 자산 조회 테스트")
    void testFindById() {
        // Given
        assetMapper.insert(testCryptoAsset);
        Long assetId = testCryptoAsset.getId();

        // When
        Optional<Asset> found = assetMapper.findById(assetId);

        // Then
        assertThat(found).isPresent();
        Asset asset = found.get();
        assertThat(asset.getSymbol()).isEqualTo("BTC");
        assertThat(asset.getName()).isEqualTo("Bitcoin");
        assertThat(asset.getAssetType()).isEqualTo(AssetType.CRYPTO);
        assertThat(asset.getExchange()).isEqualTo("upbit");
        assertThat(asset.getCountryCode()).isEqualTo("KR");
        assertThat(asset.getQuantity()).isEqualByComparingTo(new BigDecimal("1.5"));
        assertThat(asset.getAveragePrice()).isEqualByComparingTo(new BigDecimal("50000000"));
        assertThat(asset.getCurrency()).isEqualTo("KRW");
        assertThat(asset.getIsActive()).isTrue();
        assertThat(asset.getNotes()).isEqualTo("Test crypto asset");
    }

    @Test
    @Order(3)
    @DisplayName("자산 정보 수정 테스트")
    void testUpdate() {
        // Given
        assetMapper.insert(testCryptoAsset);
        Long assetId = testCryptoAsset.getId();

        // When
        testCryptoAsset.setQuantity(new BigDecimal("2.0"));
        testCryptoAsset.setAveragePrice(new BigDecimal("48000000"));
        testCryptoAsset.setNotes("Updated crypto asset");
        testCryptoAsset.setUpdatedAt(LocalDateTime.now());
        assetMapper.update(testCryptoAsset);

        // Then
        Optional<Asset> updated = assetMapper.findById(assetId);
        assertThat(updated).isPresent();
        Asset asset = updated.get();
        assertThat(asset.getQuantity()).isEqualByComparingTo(new BigDecimal("2.0"));
        assertThat(asset.getAveragePrice()).isEqualByComparingTo(new BigDecimal("48000000"));
        assertThat(asset.getNotes()).isEqualTo("Updated crypto asset");
    }

    @Test
    @Order(4)
    @DisplayName("자산 소프트 삭제 테스트")
    void testSoftDelete() {
        // Given
        assetMapper.insert(testCryptoAsset);
        Long assetId = testCryptoAsset.getId();

        // When
        assetMapper.softDelete(assetId);

        // Then
        Optional<Asset> deleted = assetMapper.findById(assetId);
        assertThat(deleted).isPresent();
        assertThat(deleted.get().getIsActive()).isFalse();
    }

    @Test
    @Order(5)
    @DisplayName("사용자의 모든 자산 조회 테스트")
    void testFindByUserId() {
        // Given
        assetMapper.insert(testCryptoAsset);
        assetMapper.insert(testStockAsset);

        // When
        List<Asset> assets = assetMapper.findByUserId(testUser.getId());

        // Then
        assertThat(assets).hasSize(2);
        assertThat(assets).extracting(Asset::getSymbol)
                .containsExactlyInAnyOrder("BTC", "AAPL");
    }

    @Test
    @Order(6)
    @DisplayName("사용자의 활성 자산만 조회 테스트")
    void testFindActiveAssetsByUserId() {
        // Given
        assetMapper.insert(testCryptoAsset);
        assetMapper.insert(testStockAsset);
        
        // 하나는 비활성화
        assetMapper.softDelete(testCryptoAsset.getId());

        // When
        List<Asset> activeAssets = assetMapper.findActiveAssetsByUserId(testUser.getId());

        // Then
        assertThat(activeAssets).hasSize(1);
        assertThat(activeAssets.get(0).getSymbol()).isEqualTo("AAPL");
        assertThat(activeAssets.get(0).getIsActive()).isTrue();
    }

    @Test
    @Order(7)
    @DisplayName("자산 타입별 조회 테스트")
    void testFindByUserIdAndAssetType() {
        // Given
        assetMapper.insert(testCryptoAsset);
        assetMapper.insert(testStockAsset);

        // When
        List<Asset> cryptoAssets = assetMapper.findByUserIdAndAssetType(
                testUser.getId(), AssetType.CRYPTO);
        List<Asset> stockAssets = assetMapper.findByUserIdAndAssetType(
                testUser.getId(), AssetType.STOCK);

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
        assetMapper.insert(testCryptoAsset);

        // When
        Optional<Asset> found = assetMapper.findByUserIdAndSymbol(
                testUser.getId(), "BTC");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getSymbol()).isEqualTo("BTC");
    }

    @Test
    @Order(9)
    @DisplayName("국가 코드별 자산 조회 테스트")
    void testFindByUserIdAndCountryCode() {
        // Given
        assetMapper.insert(testCryptoAsset); // KR
        assetMapper.insert(testStockAsset);   // US

        // When
        List<Asset> krAssets = assetMapper.findByUserIdAndCountryCode(
                testUser.getId(), "KR");
        List<Asset> usAssets = assetMapper.findByUserIdAndCountryCode(
                testUser.getId(), "US");

        // Then
        assertThat(krAssets).hasSize(1);
        assertThat(krAssets.get(0).getCountryCode()).isEqualTo("KR");
        assertThat(usAssets).hasSize(1);
        assertThat(usAssets.get(0).getCountryCode()).isEqualTo("US");
    }

    @Test
    @Order(10)
    @DisplayName("통화별 자산 조회 테스트")
    void testFindByUserIdAndCurrency() {
        // Given
        assetMapper.insert(testCryptoAsset); // KRW
        assetMapper.insert(testStockAsset);   // USD

        // When
        List<Asset> krwAssets = assetMapper.findByUserIdAndCurrency(
                testUser.getId(), "KRW");
        List<Asset> usdAssets = assetMapper.findByUserIdAndCurrency(
                testUser.getId(), "USD");

        // Then
        assertThat(krwAssets).hasSize(1);
        assertThat(krwAssets.get(0).getCurrency()).isEqualTo("KRW");
        assertThat(usdAssets).hasSize(1);
        assertThat(usdAssets.get(0).getCurrency()).isEqualTo("USD");
    }

    @Test
    @Order(11)
    @DisplayName("자산 타입별 개수 조회 테스트")
    void testCountAssetsByType() {
        // Given
        assetMapper.insert(testCryptoAsset); // CRYPTO
        assetMapper.insert(testStockAsset);  // STOCK

        // When
        int cryptoCount = assetMapper.countAssetsByType(testUser.getId(), AssetType.CRYPTO);
        int stockCount = assetMapper.countAssetsByType(testUser.getId(), AssetType.STOCK);

        // Then
        assertThat(cryptoCount).isEqualTo(1);
        assertThat(stockCount).isEqualTo(1);
    }

    @Test
    @Order(12)
    @DisplayName("총 투자 금액 계산 테스트")
    void testGetTotalInvestmentByUserId() {
        // Given
        assetMapper.insert(testCryptoAsset);
        assetMapper.insert(testStockAsset);

        // When
        BigDecimal totalInvestment = assetMapper.getTotalInvestmentByUserId(testUser.getId());

        // Then
        assertThat(totalInvestment).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    @Order(13)
    @DisplayName("활성 자산 개수 조회 테스트")
    void testCountActiveAssetsByUserId() {
        // Given
        assetMapper.insert(testCryptoAsset);
        assetMapper.insert(testStockAsset);
        
        // 하나는 비활성화
        assetMapper.softDelete(testCryptoAsset.getId());

        // When
        int count = assetMapper.countActiveAssetsByUserId(testUser.getId());

        // Then
        assertThat(count).isEqualTo(1);
    }

    @Test
    @Order(14)
    @DisplayName("자산 타입별 투자 금액 계산 테스트")
    void testGetInvestmentByAssetType() {
        // Given
        assetMapper.insert(testCryptoAsset);
        assetMapper.insert(testStockAsset);

        // When
        BigDecimal cryptoInvestment = assetMapper.getInvestmentByAssetType(
                testUser.getId(), AssetType.CRYPTO);
        BigDecimal stockInvestment = assetMapper.getInvestmentByAssetType(
                testUser.getId(), AssetType.STOCK);

        // Then
        assertThat(cryptoInvestment).isEqualByComparingTo(new BigDecimal("75000000.00"));
        assertThat(stockInvestment).isEqualByComparingTo(new BigDecimal("1505.00"));
    }

    @Test
    @Order(15)
    @DisplayName("상위 투자 자산 조회 테스트")
    void testFindTopInvestmentAssets() {
        // Given
        assetMapper.insert(testCryptoAsset);
        assetMapper.insert(testStockAsset);

        // When
        List<Asset> topAssets = assetMapper.findTopInvestmentAssets(testUser.getId(), 2);

        // Then
        assertThat(topAssets).hasSize(2);
        assertThat(topAssets.get(0).getSymbol()).isEqualTo("BTC");
    }

    @Test
    @Order(16)
    @DisplayName("보유 수량이 있는 자산만 조회 테스트")
    void testFindHoldingAssetsByUserId() {
        // Given
        Asset zeroQuantityAsset = Asset.builder()
                .userId(testUser.getId())
                .symbol("ZERO")
                .name("Zero Asset")
                .assetType(AssetType.CRYPTO)
                .exchange("upbit")
                .countryCode("KR")
                .quantity(BigDecimal.ZERO)
                .averagePrice(new BigDecimal("1000"))
                .currency("KRW")
                .isActive(true)
                .notes("Zero quantity asset")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        assetMapper.insert(testCryptoAsset);
        assetMapper.insert(zeroQuantityAsset);

        // When
        List<Asset> holdingAssets = assetMapper.findHoldingAssetsByUserId(testUser.getId());

        // Then
        assertThat(holdingAssets).hasSize(1);
        assertThat(holdingAssets.get(0).getSymbol()).isEqualTo("BTC");
    }

    @Test
    @Order(17)
    @DisplayName("특정 거래소 자산 조회 테스트")
    void testFindByUserIdAndExchange() {
        // Given
        assetMapper.insert(testCryptoAsset); // upbit
        assetMapper.insert(testStockAsset);  // NASDAQ

        // When
        List<Asset> upbitAssets = assetMapper.findByUserIdAndExchange(
                testUser.getId(), "upbit");
        List<Asset> nasdaqAssets = assetMapper.findByUserIdAndExchange(
                testUser.getId(), "NASDAQ");

        // Then
        assertThat(upbitAssets).hasSize(1);
        assertThat(upbitAssets.get(0).getSymbol()).isEqualTo("BTC");
        assertThat(nasdaqAssets).hasSize(1);
        assertThat(nasdaqAssets.get(0).getSymbol()).isEqualTo("AAPL");
    }

    @Test
    @Order(18)
    @DisplayName("빈 결과 조회 테스트")
    void testEmptyResults() {
        // When
        List<Asset> assets = assetMapper.findByUserId(testUser.getId());
        BigDecimal totalInvestment = assetMapper.getTotalInvestmentByUserId(testUser.getId());
        int count = assetMapper.countActiveAssetsByUserId(testUser.getId());

        // Then
        assertThat(assets).isEmpty();
        assertThat(totalInvestment).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(count).isEqualTo(0);
    }

    @Test
    @Order(19)
    @DisplayName("다중 사용자 데이터 격리 테스트")
    void testMultiUserDataIsolation() {
        // Given
        User anotherUser = User.builder()
                .email("another.user@example.com")
                .name("Another User")
                .role(Role.USER)
                .authProvider(AuthProvider.LOCAL)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        userMapper.insert(anotherUser);

        assetMapper.insert(testCryptoAsset);
        
        Asset anotherUserAsset = Asset.builder()
                .userId(anotherUser.getId())
                .symbol("ETH")
                .name("Ethereum")
                .assetType(AssetType.CRYPTO)
                .exchange("upbit")
                .countryCode("KR")
                .quantity(new BigDecimal("10"))
                .averagePrice(new BigDecimal("3000000"))
                .currency("KRW")
                .isActive(true)
                .notes("Another user asset")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        assetMapper.insert(anotherUserAsset);

        // When
        List<Asset> testUserAssets = assetMapper.findByUserId(testUser.getId());
        List<Asset> anotherUserAssets = assetMapper.findByUserId(anotherUser.getId());

        // Then
        assertThat(testUserAssets).hasSize(1);
        assertThat(testUserAssets.get(0).getSymbol()).isEqualTo("BTC");
        assertThat(anotherUserAssets).hasSize(1);
        assertThat(anotherUserAssets.get(0).getSymbol()).isEqualTo("ETH");
    }
}