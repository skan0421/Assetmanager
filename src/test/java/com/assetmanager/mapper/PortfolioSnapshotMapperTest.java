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
import java.util.Optional;

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
}
