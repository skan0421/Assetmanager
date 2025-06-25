package com.assetmanager.mapper;

import com.assetmanager.domain.PriceHistory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * PriceHistoryMapper 테스트 클래스 (database-schema.md 기반)
 * Phase 2.3: MyBatis Mapper 구현 - PriceHistoryMapper 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PriceHistoryMapperTest {

    @Autowired
    private PriceHistoryMapper priceHistoryMapper;

    private PriceHistory testPriceHistory;

    @BeforeEach
    void setUp() {
        testPriceHistory = PriceHistory.builder()
                .symbol("BTC")
                .exchange("UPBIT")
                .price(new BigDecimal("95000000"))
                .volume(new BigDecimal("1.5"))
                .marketCap(new BigDecimal("2000000000"))
                .highPrice(new BigDecimal("96000000"))
                .lowPrice(new BigDecimal("94000000"))
                .openPrice(new BigDecimal("94500000"))
                .closePrice(new BigDecimal("95000000"))
                .changeRate(new BigDecimal("0.5"))
                .dataSource("UPBIT")
                .priceTimestamp(LocalDateTime.now().minusMinutes(1))
                .build();
    }

    // =================
    // 기본 CRUD 테스트
    // =================

    @Test
    @Order(1)
    @DisplayName("가격 히스토리 등록 테스트")
    void testInsert() {
        // When
        priceHistoryMapper.insert(testPriceHistory);

        // Then
        assertThat(testPriceHistory.getId()).isNotNull();
        assertThat(testPriceHistory.getId()).isGreaterThan(0L);
    }

    @Test
    @Order(2)
    @DisplayName("ID로 가격 히스토리 조회 테스트")
    void testFindById() {
        // Given
        priceHistoryMapper.insert(testPriceHistory);
        Long priceHistoryId = testPriceHistory.getId();

        // When
        Optional<PriceHistory> found = priceHistoryMapper.findById(priceHistoryId);

        // Then
        assertThat(found).isPresent();
        PriceHistory history = found.get();
        assertThat(history.getSymbol()).isEqualTo("BTC");
        assertThat(history.getExchange()).isEqualTo("UPBIT");
        assertThat(history.getPrice()).isEqualByComparingTo(new BigDecimal("95000000"));
        assertThat(history.getVolume()).isEqualByComparingTo(new BigDecimal("1.5"));
        assertThat(history.getDataSource()).isEqualTo("UPBIT");
    }

    @Test
    @Order(3)
    @DisplayName("가격 히스토리 수정 테스트")
    void testUpdate() {
        // Given
        priceHistoryMapper.insert(testPriceHistory);
        Long priceHistoryId = testPriceHistory.getId();

        // When
        testPriceHistory.setPrice(new BigDecimal("95500000"));
        testPriceHistory.setVolume(new BigDecimal("2.0"));
        testPriceHistory.setChangeRate(new BigDecimal("1.5"));
        priceHistoryMapper.update(testPriceHistory);

        // Then
        Optional<PriceHistory> updated = priceHistoryMapper.findById(priceHistoryId);
        assertThat(updated).isPresent();
        PriceHistory history = updated.get();
        assertThat(history.getPrice()).isEqualByComparingTo(new BigDecimal("95500000"));
        assertThat(history.getVolume()).isEqualByComparingTo(new BigDecimal("2.0"));
        assertThat(history.getChangeRate()).isEqualByComparingTo(new BigDecimal("1.5"));
    }

    @Test
    @Order(4)
    @DisplayName("가격 히스토리 삭제 테스트")
    void testDelete() {
        // Given
        priceHistoryMapper.insert(testPriceHistory);
        Long priceHistoryId = testPriceHistory.getId();

        // When
        priceHistoryMapper.delete(priceHistoryId);

        // Then
        Optional<PriceHistory> deleted = priceHistoryMapper.findById(priceHistoryId);
        assertThat(deleted).isEmpty();
    }

    @Test
    @Order(5)
    @DisplayName("심볼별 최신 가격 조회 테스트")
    void testFindLatestBySymbol() {
        // Given
        priceHistoryMapper.insert(testPriceHistory);

        PriceHistory newerHistory = PriceHistory.builder()
                .symbol("BTC")
                .exchange("UPBIT")
                .price(new BigDecimal("96000000"))
                .volume(new BigDecimal("1.2"))
                .marketCap(new BigDecimal("2100000000"))
                .highPrice(new BigDecimal("96500000"))
                .lowPrice(new BigDecimal("95500000"))
                .openPrice(new BigDecimal("96000000"))
                .closePrice(new BigDecimal("96200000"))
                .changeRate(new BigDecimal("0.3"))
                .dataSource("UPBIT")
                .priceTimestamp(LocalDateTime.now()) // 더 최신
                .build();
        priceHistoryMapper.insert(newerHistory);

        // When
        Optional<PriceHistory> latest = priceHistoryMapper.findLatestBySymbol("BTC");

        // Then
        assertThat(latest).isPresent();
        assertThat(latest.get().getPrice()).isEqualByComparingTo(new BigDecimal("96000000"));
    }

    @Test
    @Order(6)
    @DisplayName("심볼별 제한된 개수 조회 테스트")
    void testFindBySymbolWithLimit() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        // 3개의 가격 데이터 생성 (시간 역순으로 가격을 설정)
        for (int i = 0; i < 3; i++) {
            PriceHistory history = PriceHistory.builder()
                    .symbol("BTC")
                    .exchange("UPBIT")
                    .price(new BigDecimal("95000000").add(new BigDecimal((2-i) * 100000))) // 최신일수록 높은 가격
                    .volume(new BigDecimal("1.0"))
                    .marketCap(new BigDecimal("2000000000"))
                    .highPrice(new BigDecimal("96000000"))
                    .lowPrice(new BigDecimal("94000000"))
                    .openPrice(new BigDecimal("94500000"))
                    .closePrice(new BigDecimal("95000000"))
                    .changeRate(new BigDecimal("0.5"))
                    .dataSource("UPBIT")
                    .priceTimestamp(now.minusMinutes(i))
                    .build();
            priceHistoryMapper.insert(history);
        }

        // When
        List<PriceHistory> histories = priceHistoryMapper.findBySymbolWithLimit("BTC", 2);

        // Then
        assertThat(histories).hasSize(2);
        // 최신순 정렬 확인 (시간 기준)
        assertThat(histories.get(0).getPriceTimestamp()).isAfter(histories.get(1).getPriceTimestamp());
        // 최신 데이터가 더 높은 가격을 가짐
        assertThat(histories.get(0).getPrice()).isGreaterThan(histories.get(1).getPrice());
    }

    @Test
    @Order(7)
    @DisplayName("거래소별 최신 가격 조회 테스트")
    void testFindLatestBySymbolAndExchange() {
        // Given
        priceHistoryMapper.insert(testPriceHistory); // UPBIT
        
        PriceHistory binanceHistory = PriceHistory.builder()
                .symbol("BTC")
                .exchange("BINANCE")
                .price(new BigDecimal("96500000"))
                .volume(new BigDecimal("2.0"))
                .marketCap(new BigDecimal("2100000000"))
                .highPrice(new BigDecimal("97000000"))
                .lowPrice(new BigDecimal("96000000"))
                .openPrice(new BigDecimal("96500000"))
                .closePrice(new BigDecimal("96500000"))
                .changeRate(new BigDecimal("1.0"))
                .dataSource("BINANCE")
                .priceTimestamp(LocalDateTime.now())
                .build();
        priceHistoryMapper.insert(binanceHistory);

        // When
        Optional<PriceHistory> upbitLatest = priceHistoryMapper.findLatestBySymbolAndExchange("BTC", "UPBIT");
        Optional<PriceHistory> binanceLatest = priceHistoryMapper.findLatestBySymbolAndExchange("BTC", "BINANCE");

        // Then
        assertThat(upbitLatest).isPresent();
        assertThat(upbitLatest.get().getExchange()).isEqualTo("UPBIT");
        
        assertThat(binanceLatest).isPresent();
        assertThat(binanceLatest.get().getExchange()).isEqualTo("BINANCE");
        assertThat(binanceLatest.get().getPrice()).isEqualByComparingTo(new BigDecimal("96500000"));
    }

    @Test
    @Order(8)
    @DisplayName("기간별 가격 분석 쿼리 테스트")
    void testPriceStatistics() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        // 첫 번째 가격 데이터
        testPriceHistory.setPrice(new BigDecimal("94000000"));
        testPriceHistory.setPriceTimestamp(now.minusHours(2));
        priceHistoryMapper.insert(testPriceHistory);
        
        // 두 번째 가격 데이터 (최대값)
        PriceHistory maxHistory = PriceHistory.builder()
                .symbol("BTC")
                .exchange("UPBIT")
                .price(new BigDecimal("97000000")) // 최대값
                .volume(new BigDecimal("2.0"))
                .marketCap(new BigDecimal("2200000000"))
                .highPrice(new BigDecimal("97500000"))
                .lowPrice(new BigDecimal("96500000"))
                .openPrice(new BigDecimal("96500000"))
                .closePrice(new BigDecimal("97000000"))
                .changeRate(new BigDecimal("3.0"))
                .dataSource("UPBIT")
                .priceTimestamp(now.minusHours(1))
                .build();
        priceHistoryMapper.insert(maxHistory);
        
        // 세 번째 가격 데이터 (최소값)
        PriceHistory minHistory = PriceHistory.builder()
                .symbol("BTC")
                .exchange("UPBIT")
                .price(new BigDecimal("92000000")) // 최소값
                .volume(new BigDecimal("1.5"))
                .marketCap(new BigDecimal("1850000000"))
                .highPrice(new BigDecimal("93000000"))
                .lowPrice(new BigDecimal("91500000"))
                .openPrice(new BigDecimal("93000000"))
                .closePrice(new BigDecimal("92000000"))
                .changeRate(new BigDecimal("-2.0"))
                .dataSource("UPBIT")
                .priceTimestamp(now.minusMinutes(30))
                .build();
        priceHistoryMapper.insert(minHistory);

        // When
        LocalDateTime start = now.minusHours(3);
        LocalDateTime end = now.plusHours(1);
        
        BigDecimal maxPrice = priceHistoryMapper.getMaxPriceInPeriod("BTC", start, end);
        BigDecimal minPrice = priceHistoryMapper.getMinPriceInPeriod("BTC", start, end);
        BigDecimal avgPrice = priceHistoryMapper.getAveragePriceInPeriod("BTC", start, end);
        BigDecimal totalVolume = priceHistoryMapper.getTotalVolumeInPeriod("BTC", start, end);

        // Then
        assertThat(maxPrice).isEqualByComparingTo(new BigDecimal("97000000"));
        assertThat(minPrice).isEqualByComparingTo(new BigDecimal("92000000"));
        assertThat(avgPrice).isPositive();
        assertThat(totalVolume).isEqualByComparingTo(new BigDecimal("5.0")); // 1.5 + 2.0 + 1.5
    }

    @Test
    @Order(9)
    @DisplayName("배치 삽입 테스트")
    void testInsertBatch() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        List<PriceHistory> histories = Arrays.asList(
            PriceHistory.builder()
                .symbol("BTC")
                .exchange("UPBIT")
                .price(new BigDecimal("95000000"))
                .volume(new BigDecimal("1.0"))
                .marketCap(new BigDecimal("2000000000"))
                .highPrice(new BigDecimal("95500000"))
                .lowPrice(new BigDecimal("94500000"))
                .openPrice(new BigDecimal("95000000"))
                .closePrice(new BigDecimal("95000000"))
                .changeRate(new BigDecimal("0.0"))
                .dataSource("UPBIT")
                .priceTimestamp(now.minusHours(2))
                .build(),
            PriceHistory.builder()
                .symbol("ETH")
                .exchange("UPBIT")
                .price(new BigDecimal("4500000"))
                .volume(new BigDecimal("5.0"))
                .marketCap(new BigDecimal("540000000"))
                .highPrice(new BigDecimal("4600000"))
                .lowPrice(new BigDecimal("4400000"))
                .openPrice(new BigDecimal("4500000"))
                .closePrice(new BigDecimal("4500000"))
                .changeRate(new BigDecimal("0.0"))
                .dataSource("UPBIT")
                .priceTimestamp(now.minusHours(1))
                .build()
        );

        // When
        priceHistoryMapper.insertBatch(histories);

        // Then
        Optional<PriceHistory> btcHistory = priceHistoryMapper.findLatestBySymbol("BTC");
        Optional<PriceHistory> ethHistory = priceHistoryMapper.findLatestBySymbol("ETH");
        
        assertThat(btcHistory).isPresent();
        assertThat(ethHistory).isPresent();
        assertThat(btcHistory.get().getPrice()).isEqualByComparingTo(new BigDecimal("95000000"));
        assertThat(ethHistory.get().getPrice()).isEqualByComparingTo(new BigDecimal("4500000"));
    }

    @Test
    @Order(10)
    @DisplayName("중복 데이터 검사 테스트")
    void testCountDuplicateData() {
        // Given
        LocalDateTime fixedTime = LocalDateTime.of(2025, 6, 25, 10, 0, 0); // 고정된 시간 사용
        testPriceHistory.setPriceTimestamp(fixedTime);
        priceHistoryMapper.insert(testPriceHistory);

        // When
        int duplicateCount = priceHistoryMapper.countDuplicateData(
            testPriceHistory.getSymbol(),
            testPriceHistory.getExchange(),
            fixedTime // 동일한 시간 사용
        );
        
        int noDuplicateCount = priceHistoryMapper.countDuplicateData(
            "ETH",
            "UPBIT",
            LocalDateTime.of(2025, 6, 25, 11, 0, 0) // 다른 시간
        );

        // Then
        assertThat(duplicateCount).isEqualTo(1);
        assertThat(noDuplicateCount).isEqualTo(0);
    }

    @Test
    @Order(11)
    @DisplayName("빈 결과 집합 테스트")
    void testEmptyResults() {
        // When
        Optional<PriceHistory> latest = priceHistoryMapper.findLatestBySymbol("UNKNOWN");
        List<PriceHistory> histories = priceHistoryMapper.findBySymbolWithLimit("UNKNOWN", 10);
        BigDecimal maxPrice = priceHistoryMapper.getMaxPriceInPeriod("UNKNOWN", 
            LocalDateTime.now().minusDays(1), LocalDateTime.now());

        // Then
        assertThat(latest).isEmpty();
        assertThat(histories).isEmpty();
        assertThat(maxPrice).isNull();
    }

    @Test
    @Order(12)
    @DisplayName("NULL 값 처리 테스트")
    void testNullValueHandling() {
        // Given - 일부 필드가 null인 가격 데이터
        PriceHistory historyWithNulls = PriceHistory.builder()
                .symbol("BTC")
                .exchange("UPBIT")
                .price(new BigDecimal("95000000"))
                .volume(new BigDecimal("1.5"))
                .marketCap(null) // NULL
                .highPrice(null) // NULL
                .lowPrice(null) // NULL
                .openPrice(null) // NULL
                .closePrice(null) // NULL
                .changeRate(null) // NULL
                .dataSource("UPBIT")
                .priceTimestamp(LocalDateTime.now())
                .build();

        // When
        priceHistoryMapper.insert(historyWithNulls);
        Optional<PriceHistory> found = priceHistoryMapper.findById(historyWithNulls.getId());

        // Then
        assertThat(found).isPresent();
        PriceHistory history = found.get();
        assertThat(history.getSymbol()).isEqualTo("BTC");
        assertThat(history.getPrice()).isEqualByComparingTo(new BigDecimal("95000000"));
        assertThat(history.getMarketCap()).isNull();
        assertThat(history.getHighPrice()).isNull();
        assertThat(history.getChangeRate()).isNull();
    }
}
