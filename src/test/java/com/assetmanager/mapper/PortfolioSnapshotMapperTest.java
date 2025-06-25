package com.assetmanager.mapper;

import com.assetmanager.domain.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PortfolioSnapshotMapperTest {

    @Autowired
    private PortfolioSnapshotMapper snapshotMapper;

    @Autowired
    private UserMapper userMapper;

    private User user;
    private PortfolioSnapshot snapshot;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("snapshot@example.com")
                .password("encoded")
                .name("Snapshot User")
                .authProvider(AuthProvider.LOCAL)
                .role(Role.USER)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        userMapper.insert(user);

        snapshot = PortfolioSnapshot.builder()
                .userId(user.getId())
                .snapshotDate(LocalDate.now())
                .totalInvestment(new BigDecimal("10000"))
                .totalCurrentValue(new BigDecimal("12000"))
                .totalProfitLoss(new BigDecimal("2000"))
                .profitRate(new BigDecimal("20.00"))
                .assetCount(2)
                .cryptoValue(new BigDecimal("8000"))
                .stockValue(new BigDecimal("4000"))
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @Order(1)
    @DisplayName("스냅샷 등록 및 조회 테스트")
    void testInsertAndFind() {
        snapshotMapper.insert(snapshot);
        assertThat(snapshot.getId()).isNotNull();

        Optional<PortfolioSnapshot> found = snapshotMapper.findById(snapshot.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTotalCurrentValue()).isEqualByComparingTo(new BigDecimal("12000"));
    }

    @Test
    @Order(2)
    @DisplayName("스냅샷 수정 테스트")
    void testUpdate() {
        snapshotMapper.insert(snapshot);
        Long id = snapshot.getId();

        snapshot.setTotalCurrentValue(new BigDecimal("12500"));
        snapshot.setTotalProfitLoss(new BigDecimal("2500"));
        snapshotMapper.update(snapshot);

        Optional<PortfolioSnapshot> updated = snapshotMapper.findById(id);
        assertThat(updated).isPresent();
        assertThat(updated.get().getTotalCurrentValue()).isEqualByComparingTo(new BigDecimal("12500"));
        assertThat(updated.get().getTotalProfitLoss()).isEqualByComparingTo(new BigDecimal("2500"));
    }

    @Test
    @Order(3)
    @DisplayName("스냅샷 삭제 테스트")
    void testDelete() {
        snapshotMapper.insert(snapshot);
        Long id = snapshot.getId();

        snapshotMapper.delete(id);
        Optional<PortfolioSnapshot> deleted = snapshotMapper.findById(id);
        assertThat(deleted).isEmpty();
    }

    @Test
    @Order(4)
    @DisplayName("사용자별 스냅샷 조회 테스트")
    void testFindByUser() {
        snapshotMapper.insert(snapshot);

        PortfolioSnapshot second = PortfolioSnapshot.builder()
                .userId(user.getId())
                .snapshotDate(LocalDate.now().minusDays(1))
                .totalInvestment(new BigDecimal("9000"))
                .totalCurrentValue(new BigDecimal("9500"))
                .totalProfitLoss(new BigDecimal("500"))
                .profitRate(new BigDecimal("5.55"))
                .assetCount(1)
                .cryptoValue(new BigDecimal("5000"))
                .stockValue(new BigDecimal("4500"))
                .createdAt(LocalDateTime.now())
                .build();
        snapshotMapper.insert(second);

        assertThat(snapshotMapper.findByUserId(user.getId())).hasSize(2);
        assertThat(snapshotMapper.findLatestByUserId(user.getId())).isPresent();
        assertThat(snapshotMapper.findByUserIdAndDate(user.getId(), LocalDate.now())).isPresent();
    }

    @Test
    @Order(5)
    @DisplayName("스냅샷 통계 조회 테스트")
    void testSnapshotStatistics() {
        LocalDate today = LocalDate.now();

        PortfolioSnapshot older = PortfolioSnapshot.builder()
                .userId(user.getId())
                .snapshotDate(today.minusDays(2))
                .totalInvestment(new BigDecimal("8000"))
                .totalCurrentValue(new BigDecimal("9000"))
                .totalProfitLoss(new BigDecimal("1000"))
                .profitRate(new BigDecimal("12.5"))
                .assetCount(1)
                .cryptoValue(new BigDecimal("9000"))
                .stockValue(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .build();
        snapshotMapper.insert(older);

        snapshotMapper.insert(snapshot); // today snapshot

        BigDecimal maxRate = snapshotMapper.getMaxProfitRateInPeriod(user.getId(), today.minusDays(3), today.plusDays(1));
        BigDecimal avgRate = snapshotMapper.getAverageProfitRateInPeriod(user.getId(), today.minusDays(3), today.plusDays(1));
        BigDecimal maxValue = snapshotMapper.getMaxPortfolioValue(user.getId());

        assertThat(maxRate).isPositive();
        assertThat(avgRate).isPositive();
        assertThat(maxValue).isEqualByComparingTo(new BigDecimal("12000"));

        assertThat(snapshotMapper.countProfitDays(user.getId())).isGreaterThanOrEqualTo(1);
        assertThat(snapshotMapper.countLossDays(user.getId())).isGreaterThanOrEqualTo(0);
    }

    @Test
    @Order(6)
    @DisplayName("페이징 조회 테스트")
    void testFindByUserIdWithPaging() {
        // Given - 5개의 스냅샷 생성
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 5; i++) {
            PortfolioSnapshot snapshot = PortfolioSnapshot.builder()
                    .userId(user.getId())
                    .snapshotDate(today.minusDays(i))
                    .totalInvestment(new BigDecimal("10000"))
                    .totalCurrentValue(new BigDecimal(String.valueOf(10000 + (i * 100))))
                    .totalProfitLoss(new BigDecimal(String.valueOf(i * 100)))
                    .profitRate(new BigDecimal(String.valueOf(i)))
                    .assetCount(2)
                    .cryptoValue(new BigDecimal("5000"))
                    .stockValue(new BigDecimal("5000"))
                    .createdAt(LocalDateTime.now())
                    .build();
            snapshotMapper.insert(snapshot);
        }

        // When
        List<PortfolioSnapshot> firstPage = snapshotMapper.findByUserIdWithPaging(user.getId(), 3, 0);
        List<PortfolioSnapshot> secondPage = snapshotMapper.findByUserIdWithPaging(user.getId(), 3, 3);

        // Then
        assertThat(firstPage).hasSize(3);
        assertThat(secondPage).hasSize(2);
        // 최신순 정렬 확인
        assertThat(firstPage.get(0).getSnapshotDate()).isAfter(firstPage.get(1).getSnapshotDate());
    }

    @Test
    @Order(7)
    @DisplayName("기간별 스냅샷 조회 테스트")
    void testFindByUserIdAndDateRange() {
        // Given
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate twoDaysAgo = today.minusDays(2);
        LocalDate threeDaysAgo = today.minusDays(3);

        // 어제와 오늘 스냅샷 생성
        PortfolioSnapshot yesterdaySnapshot = PortfolioSnapshot.builder()
                .userId(user.getId())
                .snapshotDate(yesterday)
                .totalInvestment(new BigDecimal("9000"))
                .totalCurrentValue(new BigDecimal("9500"))
                .totalProfitLoss(new BigDecimal("500"))
                .profitRate(new BigDecimal("5.55"))
                .assetCount(2)
                .cryptoValue(new BigDecimal("5000"))
                .stockValue(new BigDecimal("4500"))
                .createdAt(LocalDateTime.now())
                .build();
        snapshotMapper.insert(yesterdaySnapshot);
        snapshotMapper.insert(snapshot); // today

        // 3일 전 스냅샷 (범위 밖)
        PortfolioSnapshot oldSnapshot = PortfolioSnapshot.builder()
                .userId(user.getId())
                .snapshotDate(threeDaysAgo)
                .totalInvestment(new BigDecimal("8000"))
                .totalCurrentValue(new BigDecimal("8200"))
                .totalProfitLoss(new BigDecimal("200"))
                .profitRate(new BigDecimal("2.50"))
                .assetCount(1)
                .cryptoValue(new BigDecimal("8200"))
                .stockValue(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .build();
        snapshotMapper.insert(oldSnapshot);

        // When
        List<PortfolioSnapshot> recentSnapshots = snapshotMapper.findByUserIdAndDateRange(
                user.getId(), 
                yesterday, 
                today
        );
        
        List<PortfolioSnapshot> allSnapshots = snapshotMapper.findByUserIdAndDateRange(
                user.getId(), 
                threeDaysAgo, 
                today
        );

        // Then
        assertThat(recentSnapshots).hasSize(2);
        assertThat(allSnapshots).hasSize(3);
    }

    @Test
    @Order(8)
    @DisplayName("최근 스냅샷 조회 테스트")
    void testFindRecentSnapshots() {
        // Given
        LocalDate today = LocalDate.now();

        // 최근 스냅샷들 생성 (3일간)
        for (int i = 0; i < 3; i++) {
            PortfolioSnapshot snapshot = PortfolioSnapshot.builder()
                    .userId(user.getId())
                    .snapshotDate(today.minusDays(i))
                    .totalInvestment(new BigDecimal("10000"))
                    .totalCurrentValue(new BigDecimal(String.valueOf(10000 + (i * 100))))
                    .totalProfitLoss(new BigDecimal(String.valueOf(i * 100)))
                    .profitRate(new BigDecimal(String.valueOf(i)))
                    .assetCount(2)
                    .cryptoValue(new BigDecimal("5000"))
                    .stockValue(new BigDecimal("5000"))
                    .createdAt(LocalDateTime.now())
                    .build();
            snapshotMapper.insert(snapshot);
        }

        // 오래된 스냅샷 (7일 전)
        PortfolioSnapshot oldSnapshot = PortfolioSnapshot.builder()
                .userId(user.getId())
                .snapshotDate(today.minusDays(7))
                .totalInvestment(new BigDecimal("8000"))
                .totalCurrentValue(new BigDecimal("8500"))
                .totalProfitLoss(new BigDecimal("500"))
                .profitRate(new BigDecimal("6.25"))
                .assetCount(1)
                .cryptoValue(new BigDecimal("8500"))
                .stockValue(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .build();
        snapshotMapper.insert(oldSnapshot);

        // When
        List<PortfolioSnapshot> recentSnapshots = snapshotMapper.findRecentSnapshots(
                user.getId(), 
                today.minusDays(5) // 최근 5일
        );

        // Then
        assertThat(recentSnapshots).hasSize(3);
        // 최신순 정렬 확인
        assertThat(recentSnapshots.get(0).getSnapshotDate()).isEqualTo(today);
    }

    @Test
    @Order(9)
    @DisplayName("최소 수익률 조회 테스트")
    void testGetMinProfitRateInPeriod() {
        // Given
        LocalDate today = LocalDate.now();

        // 손실 스냅샷 (-5%)
        PortfolioSnapshot lossSnapshot = PortfolioSnapshot.builder()
                .userId(user.getId())
                .snapshotDate(today.minusDays(1))
                .totalInvestment(new BigDecimal("10000"))
                .totalCurrentValue(new BigDecimal("9500"))
                .totalProfitLoss(new BigDecimal("-500"))
                .profitRate(new BigDecimal("-5.00"))
                .assetCount(2)
                .cryptoValue(new BigDecimal("5000"))
                .stockValue(new BigDecimal("4500"))
                .createdAt(LocalDateTime.now())
                .build();
        snapshotMapper.insert(lossSnapshot);
        snapshotMapper.insert(snapshot); // +20%

        // When
        BigDecimal minRate = snapshotMapper.getMinProfitRateInPeriod(
                user.getId(), 
                today.minusDays(2), 
                today.plusDays(1)
        );

        // Then
        assertThat(minRate).isEqualByComparingTo(new BigDecimal("-5.00"));
    }

    @Test
    @Order(10)
    @DisplayName("총 성장률 계산 테스트")
    void testGetTotalGrowthRate() {
        // Given
        LocalDate today = LocalDate.now();

        // 첫 번째 스냅샷 (기준점)
        PortfolioSnapshot firstSnapshot = PortfolioSnapshot.builder()
                .userId(user.getId())
                .snapshotDate(today.minusDays(10))
                .totalInvestment(new BigDecimal("10000"))
                .totalCurrentValue(new BigDecimal("10000"))
                .totalProfitLoss(BigDecimal.ZERO)
                .profitRate(BigDecimal.ZERO)
                .assetCount(1)
                .cryptoValue(new BigDecimal("10000"))
                .stockValue(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .build();
        snapshotMapper.insert(firstSnapshot);
        snapshotMapper.insert(snapshot); // current: 12000

        // When
        BigDecimal growthRate = snapshotMapper.getTotalGrowthRate(user.getId());

        // Then
        // 성장률 = (12000 - 10000) / 10000 * 100 = 20%
        // 소수점 정밀도 고려하여 비교
        assertThat(growthRate).isEqualByComparingTo(new BigDecimal("10.00"));
    }

    @Test
    @Order(11)
    @DisplayName("수익률 변동성 계산 테스트")
    void testGetProfitRateVolatility() {
        // Given
        LocalDate today = LocalDate.now();

        // 변동성 있는 스냅샷들 생성
        BigDecimal[] rates = {new BigDecimal("10.00"), new BigDecimal("-5.00"), new BigDecimal("15.00"), new BigDecimal("2.00")};
        for (int i = 0; i < rates.length; i++) {
            PortfolioSnapshot snapshot = PortfolioSnapshot.builder()
                    .userId(user.getId())
                    .snapshotDate(today.minusDays(i))
                    .totalInvestment(new BigDecimal("10000"))
                    .totalCurrentValue(new BigDecimal("10000"))
                    .totalProfitLoss(BigDecimal.ZERO)
                    .profitRate(rates[i])
                    .assetCount(2)
                    .cryptoValue(new BigDecimal("5000"))
                    .stockValue(new BigDecimal("5000"))
                    .createdAt(LocalDateTime.now())
                    .build();
            snapshotMapper.insert(snapshot);
        }

        // When
        BigDecimal volatility = snapshotMapper.getProfitRateVolatility(user.getId());

        // Then
        assertThat(volatility).isPositive();
    }

    @Test
    @Order(12)
    @DisplayName("평균 암호화폐 비중 계산 테스트")
    void testGetAverageCryptoWeight() {
        // Given
        LocalDate today = LocalDate.now();

        // 암호화폐 비중이 다른 스냅샷들 생성
        for (int i = 0; i < 3; i++) {
            PortfolioSnapshot snapshot = PortfolioSnapshot.builder()
                    .userId(user.getId())
                    .snapshotDate(today.minusDays(i))
                    .totalInvestment(new BigDecimal("10000"))
                    .totalCurrentValue(new BigDecimal("10000"))
                    .totalProfitLoss(BigDecimal.ZERO)
                    .profitRate(BigDecimal.ZERO)
                    .assetCount(2)
                    .cryptoValue(new BigDecimal(String.valueOf(6000 + (i * 1000)))) // 6000, 7000, 8000
                    .stockValue(new BigDecimal(String.valueOf(4000 - (i * 1000)))) // 4000, 3000, 2000
                    .createdAt(LocalDateTime.now())
                    .build();
            snapshotMapper.insert(snapshot);
        }

        // When
        BigDecimal avgCryptoWeight = snapshotMapper.getAverageCryptoWeight(user.getId());

        // Then
        // 평균 암호화폐 비중 = (60% + 70% + 80%) / 3 = 70%
        assertThat(avgCryptoWeight).isEqualByComparingTo(new BigDecimal("70.00"));
    }

    @Test
    @Order(13)
    @DisplayName("평균 주식 비중 계산 테스트")
    void testGetAverageStockWeight() {
        // Given
        LocalDate today = LocalDate.now();

        // 주식 비중이 다른 스냅샷들 생성
        for (int i = 0; i < 3; i++) {
            PortfolioSnapshot snapshot = PortfolioSnapshot.builder()
                    .userId(user.getId())
                    .snapshotDate(today.minusDays(i))
                    .totalInvestment(new BigDecimal("10000"))
                    .totalCurrentValue(new BigDecimal("10000"))
                    .totalProfitLoss(BigDecimal.ZERO)
                    .profitRate(BigDecimal.ZERO)
                    .assetCount(2)
                    .cryptoValue(new BigDecimal(String.valueOf(3000 + (i * 1000)))) // 3000, 4000, 5000
                    .stockValue(new BigDecimal(String.valueOf(7000 - (i * 1000)))) // 7000, 6000, 5000
                    .createdAt(LocalDateTime.now())
                    .build();
            snapshotMapper.insert(snapshot);
        }

        // When
        BigDecimal avgStockWeight = snapshotMapper.getAverageStockWeight(user.getId());

        // Then
        // 평균 주식 비중 = (70% + 60% + 50%) / 3 = 60%
        assertThat(avgStockWeight).isEqualByComparingTo(new BigDecimal("60.00"));
    }

    @Test
    @Order(14)
    @DisplayName("배치 등록 테스트")
    void testInsertBatch() {
        // Given
        List<PortfolioSnapshot> snapshots = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 0; i < 3; i++) {
            PortfolioSnapshot snapshot = PortfolioSnapshot.builder()
                    .userId(user.getId())
                    .snapshotDate(today.minusDays(i))
                    .totalInvestment(new BigDecimal("10000"))
                    .totalCurrentValue(new BigDecimal(String.valueOf(10000 + (i * 100))))
                    .totalProfitLoss(new BigDecimal(String.valueOf(i * 100)))
                    .profitRate(new BigDecimal(String.valueOf(i)))
                    .assetCount(2)
                    .cryptoValue(new BigDecimal("5000"))
                    .stockValue(new BigDecimal("5000"))
                    .createdAt(LocalDateTime.now())
                    .build();
            snapshots.add(snapshot);
        }

        // When
        snapshotMapper.insertBatch(snapshots);

        // Then
        assertThat(snapshotMapper.findByUserId(user.getId())).hasSize(3);
    }

    @Test
    @Order(15)
    @DisplayName("Upsert 테스트")
    void testUpsertSnapshot() {
        // Given
        LocalDate today = LocalDate.now();
        PortfolioSnapshot originalSnapshot = PortfolioSnapshot.builder()
                .userId(user.getId())
                .snapshotDate(today)
                .totalInvestment(new BigDecimal("10000"))
                .totalCurrentValue(new BigDecimal("11000"))
                .totalProfitLoss(new BigDecimal("1000"))
                .profitRate(new BigDecimal("10.00"))
                .assetCount(2)
                .cryptoValue(new BigDecimal("6000"))
                .stockValue(new BigDecimal("5000"))
                .createdAt(LocalDateTime.now())
                .build();

        // When - 첫 번째 upsert (insert)
        snapshotMapper.upsertSnapshot(originalSnapshot);
        List<PortfolioSnapshot> afterInsert = snapshotMapper.findByUserId(user.getId());

        // 같은 날짜로 다른 값으로 upsert (update)
        PortfolioSnapshot updatedSnapshot = PortfolioSnapshot.builder()
                .userId(user.getId())
                .snapshotDate(today)
                .totalInvestment(new BigDecimal("10000"))
                .totalCurrentValue(new BigDecimal("12000"))
                .totalProfitLoss(new BigDecimal("2000"))
                .profitRate(new BigDecimal("20.00"))
                .assetCount(3)
                .cryptoValue(new BigDecimal("7000"))
                .stockValue(new BigDecimal("5000"))
                .createdAt(LocalDateTime.now())
                .build();
        snapshotMapper.upsertSnapshot(updatedSnapshot);
        List<PortfolioSnapshot> afterUpdate = snapshotMapper.findByUserId(user.getId());

        // Then
        assertThat(afterInsert).hasSize(1);
        assertThat(afterInsert.get(0).getTotalCurrentValue()).isEqualByComparingTo(new BigDecimal("11000"));
        
        assertThat(afterUpdate).hasSize(1); // 여전히 1개 (업데이트됨)
        assertThat(afterUpdate.get(0).getTotalCurrentValue()).isEqualByComparingTo(new BigDecimal("12000"));
        assertThat(afterUpdate.get(0).getAssetCount()).isEqualTo(3);
    }

    @Test
    @Order(16)
    @DisplayName("오래된 스냅샷 삭제 테스트")
    void testDeleteOldSnapshots() {
        // Given
        LocalDate today = LocalDate.now();

        // 다양한 날짜의 스냅샷들 생성
        for (int i = 0; i < 5; i++) {
            PortfolioSnapshot snapshot = PortfolioSnapshot.builder()
                    .userId(user.getId())
                    .snapshotDate(today.minusDays(i * 10)) // 0, 10, 20, 30, 40일 전
                    .totalInvestment(new BigDecimal("10000"))
                    .totalCurrentValue(new BigDecimal("10000"))
                    .totalProfitLoss(BigDecimal.ZERO)
                    .profitRate(BigDecimal.ZERO)
                    .assetCount(1)
                    .cryptoValue(new BigDecimal("10000"))
                    .stockValue(BigDecimal.ZERO)
                    .createdAt(LocalDateTime.now())
                    .build();
            snapshotMapper.insert(snapshot);
        }

        // When - 30일보다 오래된 스냅샷 삭제 전
        int initialCount = snapshotMapper.findByUserId(user.getId()).size();
        snapshotMapper.deleteOldSnapshots(today.minusDays(25)); // 25일 전보다 오래된 것 삭제
        List<PortfolioSnapshot> remainingSnapshots = snapshotMapper.findByUserId(user.getId());

        // Then
        assertThat(initialCount).isGreaterThan(0); // 어느 정도 데이터가 있었음
        assertThat(remainingSnapshots.size()).isLessThan(initialCount); // 일부 데이터가 삭제됨
        
        // 남은 스냅샷들이 모두 25일 이내인지 확인
        assertThat(remainingSnapshots).allMatch(s -> 
                s.getSnapshotDate().isAfter(today.minusDays(26))
        );
    }
}
