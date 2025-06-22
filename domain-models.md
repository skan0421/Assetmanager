# 🏗️ Domain 모델 상세 설계

## 📋 **설계 원칙**

### **POJO 기반 Domain 모델**
1. **순수 Java 객체**: JPA 어노테이션 없는 POJO
2. **비즈니스 로직 포함**: 도메인 규칙을 모델 내부에 구현
3. **불변성 보장**: 가능한 한 불변 객체로 설계
4. **책임 분리**: 각 도메인의 명확한 책임 정의

---

## 👤 **User 도메인 모델**

> 상세 구현 내용은 파일 크기 제한으로 요약만 표시
> 실제 구현시 아래 비즈니스 로직 메서드들이 포함됨

```java
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    // 기본 필드들
    private Long id;
    private String email;
    private String password;
    private String name;
    private AuthProvider authProvider;
    private Role role;
    private Boolean isActive;
    
    // 비즈니스 로직 메서드
    public boolean isSocialLogin();
    public boolean isAdmin();
    public void updateLastLogin();
    public boolean isAccountActive();
    public void changeEmail(String newEmail);
}
```

---

## 💰 **Asset 도메인 모델**

```java
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Asset {
    // 기본 필드들
    private Long id;
    private Long userId;
    private String symbol;
    private String name;
    private AssetType assetType;
    private String exchange;
    private BigDecimal quantity;
    private BigDecimal averagePrice;
    
    // 핵심 비즈니스 로직 메서드
    public BigDecimal getTotalInvestmentAmount();
    public BigDecimal getCurrentValue(BigDecimal currentPrice);
    public BigDecimal getProfitLoss(BigDecimal currentPrice);
    public BigDecimal getProfitRate(BigDecimal currentPrice);
    public void addPurchase(BigDecimal buyQuantity, BigDecimal buyPrice);
    public BigDecimal sell(BigDecimal sellQuantity);
    public boolean isHolding();
    public BigDecimal getPortfolioWeight(BigDecimal totalPortfolioValue);
}
```

---

## 📊 **Transaction 도메인 모델**

```java
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Transaction {
    // 기본 필드들
    private Long id;
    private Long userId;
    private Long assetId;
    private TransactionType transactionType;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal totalAmount;
    private BigDecimal fee;
    private BigDecimal tax;
    
    // 비즈니스 로직 메서드
    public BigDecimal calculateNetAmount();
    public boolean isBuyTransaction();
    public boolean isSellTransaction();
    public BigDecimal getFeeRate();
    public void validate();
    public String getSummary();
}
```

---

## 📈 **PriceHistory 도메인 모델**

```java
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PriceHistory {
    // 기본 필드들
    private Long id;
    private String symbol;
    private String exchange;
    private BigDecimal price;
    private BigDecimal volume;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal openPrice;
    private BigDecimal closePrice;
    private BigDecimal changeRate;
    
    // 비즈니스 로직 메서드
    public boolean hasPriceChanged();
    public boolean isPriceIncreased();
    public boolean isPriceDecreased();
    public BigDecimal getPriceRange();
    public BigDecimal getIntraDayChangeRate();
    public BigDecimal getVolatilityIndicator();
    public boolean hasValidOHLCData();
    public boolean isRecentData();
}
```

---

## 🗝️ **ApiKey 도메인 모델**

```java
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ApiKey {
    // 기본 필드들
    private Long id;
    private Long userId;
    private ExchangeType exchangeType;
    private String exchangeName;
    private String accessKey;  // 암호화된 상태
    private String secretKey;  // 암호화된 상태
    private Map<String, Object> apiPermissions;
    private Boolean isActive;
    
    // 비즈니스 로직 메서드
    public boolean isActiveAndValid();
    public boolean isExpired();
    public void updateLastUsed();
    public void deactivate();
    public boolean hasPermission(String permission);
    public boolean canTrade();
    public boolean canRead();
    public boolean isCryptoExchange();
    public boolean isStockBroker();
}
```

---

## 📊 **PortfolioSnapshot 도메인 모델**

```java
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PortfolioSnapshot {
    // 기본 필드들
    private Long id;
    private Long userId;
    private LocalDate snapshotDate;
    private BigDecimal totalInvestment;
    private BigDecimal totalCurrentValue;
    private BigDecimal totalProfitLoss;
    private BigDecimal profitRate;
    private Integer assetCount;
    private BigDecimal cryptoValue;
    private BigDecimal stockValue;
    
    // 비즈니스 로직 메서드
    public void recalculateProfitRate();
    public void recalculateProfitLoss();
    public boolean isProfit();
    public boolean isLoss();
    public BigDecimal getCryptoWeight();
    public BigDecimal getStockWeight();
    public BigDecimal getDiversificationIndex();
    public String getSummary();
    public boolean isTodaySnapshot();
}
```

---

## 🔧 **MyBatis Mapper 인터페이스 예시**

### **UserMapper 인터페이스**

```java
@Mapper
public interface UserMapper {
    
    // 기본 CRUD
    @Insert("INSERT INTO users (email, password, name, auth_provider, role) " +
            "VALUES (#{email}, #{password}, #{name}, #{authProvider}, #{role})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(User user);
    
    @Select("SELECT * FROM users WHERE id = #{id}")
    Optional<User> findById(Long id);
    
    @Select("SELECT * FROM users WHERE email = #{email}")
    Optional<User> findByEmail(String email);
    
    @Update("UPDATE users SET name = #{name}, updated_at = NOW() WHERE id = #{id}")
    void update(User user);
    
    @Delete("UPDATE users SET is_active = false WHERE id = #{id}")
    void softDelete(Long id);
    
    // 비즈니스 쿼리
    @Select("SELECT * FROM users WHERE auth_provider = #{authProvider}")
    List<User> findByAuthProvider(@Param("authProvider") User.AuthProvider authProvider);
    
    @Select("SELECT COUNT(*) FROM users WHERE is_active = true")
    int countActiveUsers();
    
    @Update("UPDATE users SET last_login_at = NOW() WHERE id = #{id}")
    void updateLastLogin(Long id);
}
```

### **AssetMapper 인터페이스**

```java
@Mapper
public interface AssetMapper {
    
    // 기본 CRUD - XML에서 구현
    void insert(Asset asset);
    Asset findById(Long id);
    void update(Asset asset);
    void delete(Long id);
    
    // 사용자별 자산 조회
    List<Asset> findByUserId(Long userId);
    
    // 활성 자산만 조회
    @Select("SELECT * FROM assets WHERE user_id = #{userId} AND is_active = true")
    List<Asset> findActiveAssetsByUserId(Long userId);
    
    // 자산 타입별 조회
    @Select("SELECT * FROM assets WHERE user_id = #{userId} AND asset_type = #{assetType}")
    List<Asset> findByUserIdAndAssetType(@Param("userId") Long userId, 
                                        @Param("assetType") Asset.AssetType assetType);
    
    // 포트폴리오 계산용 쿼리
    @Select("SELECT COALESCE(SUM(quantity * average_price), 0) " +
            "FROM assets WHERE user_id = #{userId} AND is_active = true")
    BigDecimal getTotalInvestmentByUserId(Long userId);
    
    @Select("SELECT COUNT(*) FROM assets WHERE user_id = #{userId} AND is_active = true")
    int countActiveAssetsByUserId(Long userId);
}
```

---

## 🎯 **Domain 모델 설계 특징**

### **1. 비즈니스 로직 중심**
- 각 도메인 모델에 해당 영역의 핵심 비즈니스 로직 포함
- 단순한 getter/setter가 아닌 의미있는 메서드 제공
- 도메인 규칙을 코드로 명확하게 표현

### **2. 유효성 검사 내장**
- 도메인 객체 자체에서 데이터 유효성 검사
- 예외 상황에 대한 명확한 에러 메시지
- 비즈니스 규칙 위반 방지

### **3. 계산 로직 캡슐화**
- 포트폴리오 수익률, 손익 계산 등 복잡한 로직을 도메인 내부에 구현
- 외부에서는 단순한 메서드 호출로 결과 획득
- 계산 로직 변경시 도메인 모델만 수정하면 됨

### **4. MyBatis 최적화**
- SQL 쿼리를 직접 제어할 수 있는 구조
- 복잡한 집계 쿼리나 성능 최적화가 필요한 부분은 XML 매퍼에서 구현
- 간단한 쿼리는 어노테이션으로 처리

---

*Domain 모델 설계 완료: 2025-06-22*
*6개 주요 도메인 모델 + 비즈니스 로직 구현*
*MyBatis Mapper 인터페이스 예시 포함*
