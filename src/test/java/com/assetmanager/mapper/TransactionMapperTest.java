package com.assetmanager.mapper;

import com.assetmanager.domain.*;
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
 * TransactionMapper 테스트 클래스 (database-schema.md 기반)
 * Phase 2.3: MyBatis Mapper 구현 - TransactionMapper 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TransactionMapperTest {

    @Autowired
    private TransactionMapper transactionMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private AssetMapper assetMapper;

    private User testUser;
    private Asset testAsset;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = User.builder()
                .email("test@example.com")
                .password("encoded_password123")
                .name("Test User")
                .authProvider(AuthProvider.LOCAL)
                .role(Role.USER)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        userMapper.insert(testUser);

        // 테스트 자산 생성
        testAsset = Asset.builder()
                .userId(testUser.getId())
                .symbol("AAPL")
                .name("Apple Inc.")
                .assetType(AssetType.STOCK)
                .exchange("NASDAQ")
                .quantity(new BigDecimal("100.00000000"))
                .averagePrice(new BigDecimal("150.00"))
                .currency("USD")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        assetMapper.insert(testAsset);

        // 테스트 거래 내역 생성
        testTransaction = Transaction.builder()
                .userId(testUser.getId())
                .assetId(testAsset.getId())
                .transactionType(TransactionType.BUY)
                .quantity(new BigDecimal("50.00000000"))
                .price(new BigDecimal("145.00"))
                .totalAmount(new BigDecimal("7250.00"))
                .fee(new BigDecimal("10.00"))
                .tax(new BigDecimal("5.00"))
                .netAmount(new BigDecimal("7265.00"))
                .transactionDate(LocalDateTime.now().minusDays(1))
                .notes("Test buy transaction")
                .externalId("EXT001")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // =================
    // 기본 CRUD 테스트
    // =================

    @Test
    @Order(1)
    @DisplayName("거래 내역 등록 테스트")
    void testInsert() {
        // When
        transactionMapper.insert(testTransaction);

        // Then
        assertThat(testTransaction.getId()).isNotNull();
        assertThat(testTransaction.getId()).isGreaterThan(0L);
    }

    @Test
    @Order(2)
    @DisplayName("ID로 거래 내역 조회 테스트")
    void testFindById() {
        // Given
        transactionMapper.insert(testTransaction);
        Long transactionId = testTransaction.getId();

        // When
        Optional<Transaction> found = transactionMapper.findById(transactionId);

        // Then
        assertThat(found).isPresent();
        Transaction transaction = found.get();
        assertThat(transaction.getUserId()).isEqualTo(testUser.getId());
        assertThat(transaction.getAssetId()).isEqualTo(testAsset.getId());
        assertThat(transaction.getTransactionType()).isEqualTo(TransactionType.BUY);
        assertThat(transaction.getQuantity()).isEqualByComparingTo(new BigDecimal("50.00000000"));
        assertThat(transaction.getPrice()).isEqualByComparingTo(new BigDecimal("145.00"));
        assertThat(transaction.getTotalAmount()).isEqualByComparingTo(new BigDecimal("7250.00"));
        assertThat(transaction.getFee()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(transaction.getTax()).isEqualByComparingTo(new BigDecimal("5.00"));
        assertThat(transaction.getNetAmount()).isEqualByComparingTo(new BigDecimal("7265.00"));
        assertThat(transaction.getNotes()).isEqualTo("Test buy transaction");
        assertThat(transaction.getExternalId()).isEqualTo("EXT001");
    }

    @Test
    @Order(3)
    @DisplayName("존재하지 않는 ID 조회 테스트")
    void testFindByIdNotFound() {
        // When
        Optional<Transaction> found = transactionMapper.findById(999L);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @Order(4)
    @DisplayName("거래 내역 수정 테스트")
    void testUpdate() {
        // Given
        transactionMapper.insert(testTransaction);
        Long transactionId = testTransaction.getId();

        // When
        testTransaction.setQuantity(new BigDecimal("75.00000000"));
        testTransaction.setPrice(new BigDecimal("150.00"));
        testTransaction.setTotalAmount(new BigDecimal("11250.00"));
        testTransaction.setFee(new BigDecimal("15.00"));
        testTransaction.setNotes("Updated transaction");
        testTransaction.setUpdatedAt(LocalDateTime.now());
        transactionMapper.update(testTransaction);

        // Then
        Optional<Transaction> updated = transactionMapper.findById(transactionId);
        assertThat(updated).isPresent();
        Transaction transaction = updated.get();
        assertThat(transaction.getQuantity()).isEqualByComparingTo(new BigDecimal("75.00000000"));
        assertThat(transaction.getPrice()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(transaction.getTotalAmount()).isEqualByComparingTo(new BigDecimal("11250.00"));
        assertThat(transaction.getFee()).isEqualByComparingTo(new BigDecimal("15.00"));
        assertThat(transaction.getNotes()).isEqualTo("Updated transaction");
    }

    @Test
    @Order(5)
    @DisplayName("거래 내역 삭제 테스트")
    void testDelete() {
        // Given
        transactionMapper.insert(testTransaction);
        Long transactionId = testTransaction.getId();

        // When
        transactionMapper.delete(transactionId);

        // Then
        Optional<Transaction> deleted = transactionMapper.findById(transactionId);
        assertThat(deleted).isEmpty();
    }

    @Test
    @Order(6)
    @DisplayName("사용자의 모든 거래 내역 조회 테스트")
    void testFindByUserId() {
        // Given
        transactionMapper.insert(testTransaction);
        
        // 두 번째 거래 생성
        Transaction secondTransaction = Transaction.builder()
                .userId(testUser.getId())
                .assetId(testAsset.getId())
                .transactionType(TransactionType.SELL)
                .quantity(new BigDecimal("25.00000000"))
                .price(new BigDecimal("155.00"))
                .totalAmount(new BigDecimal("3875.00"))
                .fee(new BigDecimal("5.00"))
                .tax(new BigDecimal("2.50"))
                .netAmount(new BigDecimal("3867.50"))
                .transactionDate(LocalDateTime.now())
                .notes("Test sell transaction")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        transactionMapper.insert(secondTransaction);

        // When
        List<Transaction> transactions = transactionMapper.findByUserId(testUser.getId());

        // Then
        assertThat(transactions).hasSize(2);
        // 최신순 정렬 확인 (SELL 거래가 먼저)
        assertThat(transactions.get(0).getTransactionType()).isEqualTo(TransactionType.SELL);
        assertThat(transactions.get(1).getTransactionType()).isEqualTo(TransactionType.BUY);
    }

    @Test
    @Order(7)
    @DisplayName("거래 타입별 조회 테스트")
    void testFindByUserIdAndTransactionType() {
        // Given
        transactionMapper.insert(testTransaction); // BUY
        
        Transaction sellTransaction = Transaction.builder()
                .userId(testUser.getId())
                .assetId(testAsset.getId())
                .transactionType(TransactionType.SELL)
                .quantity(new BigDecimal("25.00000000"))
                .price(new BigDecimal("155.00"))
                .totalAmount(new BigDecimal("3875.00"))
                .fee(new BigDecimal("5.00"))
                .tax(new BigDecimal("2.50"))
                .netAmount(new BigDecimal("3867.50"))
                .transactionDate(LocalDateTime.now())
                .notes("Sell transaction")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        transactionMapper.insert(sellTransaction);

        // When
        List<Transaction> buyTransactions = transactionMapper.findByUserIdAndTransactionType(testUser.getId(), TransactionType.BUY);
        List<Transaction> sellTransactions = transactionMapper.findByUserIdAndTransactionType(testUser.getId(), TransactionType.SELL);

        // Then
        assertThat(buyTransactions).hasSize(1);
        assertThat(buyTransactions.get(0).getTransactionType()).isEqualTo(TransactionType.BUY);
        
        assertThat(sellTransactions).hasSize(1);
        assertThat(sellTransactions.get(0).getTransactionType()).isEqualTo(TransactionType.SELL);
    }

    @Test
    @Order(8)
    @DisplayName("총 거래 횟수 테스트")
    void testCountTransactionsByUserId() {
        // Given
        transactionMapper.insert(testTransaction);
        
        Transaction secondTransaction = Transaction.builder()
                .userId(testUser.getId())
                .assetId(testAsset.getId())
                .transactionType(TransactionType.SELL)
                .quantity(new BigDecimal("25.00000000"))
                .price(new BigDecimal("155.00"))
                .totalAmount(new BigDecimal("3875.00"))
                .fee(new BigDecimal("5.00"))
                .tax(new BigDecimal("2.50"))
                .netAmount(new BigDecimal("3867.50"))
                .transactionDate(LocalDateTime.now())
                .notes("Second transaction")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        transactionMapper.insert(secondTransaction);

        // When
        int count = transactionMapper.countTransactionsByUserId(testUser.getId());

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @Order(9)
    @DisplayName("총 매수 금액 계산 테스트")
    void testGetTotalBuyAmountByUserId() {
        // Given
        transactionMapper.insert(testTransaction); // BUY: 7250.00
        
        Transaction buyTransaction2 = Transaction.builder()
                .userId(testUser.getId())
                .assetId(testAsset.getId())
                .transactionType(TransactionType.BUY)
                .quantity(new BigDecimal("30.00000000"))
                .price(new BigDecimal("160.00"))
                .totalAmount(new BigDecimal("4800.00"))
                .fee(new BigDecimal("8.00"))
                .tax(new BigDecimal("4.00"))
                .netAmount(new BigDecimal("4812.00"))
                .transactionDate(LocalDateTime.now())
                .notes("Second buy")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        transactionMapper.insert(buyTransaction2);

        // When
        BigDecimal totalBuyAmount = transactionMapper.getTotalBuyAmountByUserId(testUser.getId());

        // Then
        assertThat(totalBuyAmount).isEqualByComparingTo(new BigDecimal("12050.00")); // 7250 + 4800
    }

    @Test
    @Order(10)
    @DisplayName("특정 자산의 평균 매수가 계산 테스트")
    void testGetAveragePurchasePrice() {
        // Given
        // 첫 번째 매수: 50주 @ 145.00 = 7250.00
        transactionMapper.insert(testTransaction);
        
        // 두 번째 매수: 30주 @ 160.00 = 4800.00
        Transaction buyTransaction2 = Transaction.builder()
                .userId(testUser.getId())
                .assetId(testAsset.getId())
                .transactionType(TransactionType.BUY)
                .quantity(new BigDecimal("30.00000000"))
                .price(new BigDecimal("160.00"))
                .totalAmount(new BigDecimal("4800.00"))
                .fee(new BigDecimal("8.00"))
                .tax(new BigDecimal("4.00"))
                .netAmount(new BigDecimal("4812.00"))
                .transactionDate(LocalDateTime.now())
                .notes("Second buy")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        transactionMapper.insert(buyTransaction2);

        // When
        BigDecimal averagePrice = transactionMapper.getAveragePurchasePrice(testUser.getId(), testAsset.getId());

        // Then
        // 평균 매수가 = (7250 + 4800) / (50 + 30) = 12050 / 80 = 150.625
        assertThat(averagePrice).isEqualByComparingTo(new BigDecimal("150.625"));
    }

    @Test
    @Order(11)
    @DisplayName("빈 결과 집합 테스트")
    void testEmptyResults() {
        // When
        List<Transaction> transactions = transactionMapper.findByUserId(testUser.getId());
        List<Transaction> buyTransactions = transactionMapper.findByUserIdAndTransactionType(testUser.getId(), TransactionType.BUY);
        List<Transaction> sellTransactions = transactionMapper.findByUserIdAndTransactionType(testUser.getId(), TransactionType.SELL);

        // Then
        assertThat(transactions).isEmpty();
        assertThat(buyTransactions).isEmpty();
        assertThat(sellTransactions).isEmpty();
    }

    @Test
    @Order(12)
    @DisplayName("NULL 값 처리 테스트")
    void testNullValueHandling() {
        // Given - notes와 externalId가 null인 거래
        Transaction transactionWithNulls = Transaction.builder()
                .userId(testUser.getId())
                .assetId(testAsset.getId())
                .transactionType(TransactionType.BUY)
                .quantity(new BigDecimal("50.00000000"))
                .price(new BigDecimal("145.00"))
                .totalAmount(new BigDecimal("7250.00"))
                .fee(new BigDecimal("10.00"))
                .tax(new BigDecimal("5.00"))
                .netAmount(new BigDecimal("7265.00"))
                .transactionDate(LocalDateTime.now())
                .notes(null) // NULL
                .externalId(null) // NULL
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // When
        transactionMapper.insert(transactionWithNulls);
        Optional<Transaction> found = transactionMapper.findById(transactionWithNulls.getId());

        // Then
        assertThat(found).isPresent();
        Transaction transaction = found.get();
        assertThat(transaction.getNotes()).isNull();
        assertThat(transaction.getExternalId()).isNull();
        assertThat(transaction.getQuantity()).isEqualByComparingTo(new BigDecimal("50.00000000"));
    }

    @Test
    @Order(13)
    @DisplayName("BigDecimal 정밀도 테스트")
    void testBigDecimalPrecision() {
        // Given - 높은 정밀도의 암호화폐 거래
        Transaction cryptoTransaction = Transaction.builder()
                .userId(testUser.getId())
                .assetId(testAsset.getId())
                .transactionType(TransactionType.BUY)
                .quantity(new BigDecimal("0.12345678")) // 8자리 소수
                .price(new BigDecimal("65432.12345678")) // 높은 정밀도 가격
                .totalAmount(new BigDecimal("8070.059139776")) // 계산된 총액
                .fee(new BigDecimal("0.00123456"))
                .tax(new BigDecimal("0.00098765"))
                .netAmount(new BigDecimal("8070.06136199"))
                .transactionDate(LocalDateTime.now())
                .notes("High precision crypto transaction")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // When
        transactionMapper.insert(cryptoTransaction);
        Optional<Transaction> found = transactionMapper.findById(cryptoTransaction.getId());

        // Then
        assertThat(found).isPresent();
        Transaction transaction = found.get();
        assertThat(transaction.getQuantity()).isEqualByComparingTo(new BigDecimal("0.12345678"));
        assertThat(transaction.getPrice()).isEqualByComparingTo(new BigDecimal("65432.12345678"));
        assertThat(transaction.getTotalAmount()).isEqualByComparingTo(new BigDecimal("8070.059139776"));
        assertThat(transaction.getFee()).isEqualByComparingTo(new BigDecimal("0.00123456"));
        assertThat(transaction.getTax()).isEqualByComparingTo(new BigDecimal("0.00098765"));
        assertThat(transaction.getNetAmount()).isEqualByComparingTo(new BigDecimal("8070.06136199"));
    }
}
