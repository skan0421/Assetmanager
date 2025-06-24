package com.assetmanager.mapper;

import com.assetmanager.domain.PriceHistory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PriceHistoryMapperTest {

    @Autowired
    private PriceHistoryMapper priceHistoryMapper;

    private PriceHistory history;

    @BeforeEach
    void setUp() {
        history = PriceHistory.builder()
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

    @Test
    @Order(1)
    @DisplayName("가격 히스토리 등록 및 조회 테스트")
    void testInsertAndFind() {
        priceHistoryMapper.insert(history);
        assertThat(history.getId()).isNotNull();

        Optional<PriceHistory> found = priceHistoryMapper.findById(history.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getSymbol()).isEqualTo("BTC");
    }

    @Test
    @Order(2)
    @DisplayName("가격 히스토리 수정 테스트")
    void testUpdate() {
        priceHistoryMapper.insert(history);
        Long id = history.getId();

        history.setPrice(new BigDecimal("95500000"));
        history.setVolume(new BigDecimal("2.0"));
        priceHistoryMapper.update(history);

        Optional<PriceHistory> updated = priceHistoryMapper.findById(id);
        assertThat(updated).isPresent();
        assertThat(updated.get().getPrice()).isEqualByComparingTo(new BigDecimal("95500000"));
        assertThat(updated.get().getVolume()).isEqualByComparingTo(new BigDecimal("2.0"));
    }

    @Test
    @Order(3)
    @DisplayName("가격 히스토리 삭제 테스트")
    void testDelete() {
        priceHistoryMapper.insert(history);
        Long id = history.getId();

        priceHistoryMapper.delete(id);
        Optional<PriceHistory> deleted = priceHistoryMapper.findById(id);
        assertThat(deleted).isEmpty();
    }

    @Test
    @Order(4)
    @DisplayName("심볼별 최신 가격 조회 테스트")
    void testFindLatestBySymbol() {
        priceHistoryMapper.insert(history);

        PriceHistory newer = PriceHistory.builder()
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
                .priceTimestamp(LocalDateTime.now())
                .build();
        priceHistoryMapper.insert(newer);

        Optional<PriceHistory> latest = priceHistoryMapper.findLatestBySymbol("BTC");
        assertThat(latest).isPresent();
        assertThat(latest.get().getPrice()).isEqualByComparingTo(new BigDecimal("96000000"));
    }

    @Test
    @Order(5)
    @DisplayName("기간별 가격 데이터 조회 테스트")
    void testFindByDateRangeAndStats() {
        LocalDateTime now = LocalDateTime.now();

        history.setPriceTimestamp(now.minusHours(2));
        priceHistoryMapper.insert(history);

        PriceHistory second = PriceHistory.builder()
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
                .priceTimestamp(now.minusHours(1))
                .build();
        priceHistoryMapper.insert(second);

        BigDecimal max = priceHistoryMapper.getMaxPriceInPeriod("BTC", now.minusHours(3), now);
        BigDecimal avg = priceHistoryMapper.getAveragePriceInPeriod("BTC", now.minusHours(3), now);

        assertThat(max).isEqualByComparingTo(new BigDecimal("96000000"));
        assertThat(avg).isPositive();

        assertThat(priceHistoryMapper.findBySymbolAndDateRange("BTC", now.minusHours(3), now)).hasSize(2);
    }
}
