# 📊 포트폴리오 통합 관리 시스템 v2.0 - 완전한 구현 계획

## 🎯 프로젝트 개요
- **프로젝트명**: Asset Manager - 포트폴리오 통합 관리 시스템 (PMS v2.0)
- **버전**: v2.0.0 (완전 재구축)
- **개발 시작**: 2025-06-19
- **개발 기간**: 2025.06 ~ 2025.08 (3개월)
- **프로젝트 타입**: 웹 애플리케이션

## 🔄 재구축 이유
- 기존 프로젝트의 복잡성과 스키마 불일치 문제 해결
- 최신 Spring Boot 3.x 기반으로 깨끗한 구조 구축
- Docker 기반 개발환경부터 배포까지 일관된 환경 구축
- 단계별 점진적 개발 접근법 적용

## 🛠️ 기술 스택

### Backend
| 기술 | 버전 | 목적 | 상태 |
|------|------|------|------|
| **Java** | 17 | 메인 개발 언어 | ✅ |
| **Spring Boot** | 3.3.0 | 웹 프레임워크 | 📋 |
| **Spring Data JPA** | 3.3.0 | ORM | 📋 |
| **Spring Security** | 6.x | 인증/보안 | 📋 |
| **MySQL** | 8.0 | 메인 데이터베이스 | 📋 |
| **Redis** | 7.0 | 캐싱 | 📋 |
| **Docker** | Latest | 컨테이너화 | 📋 |

### Frontend
| 기술 | 목적 | 상태 |
|------|------|------|
| **HTML5 + CSS3** | 기본 UI | 📋 |
| **JavaScript (Vanilla)** | 인터랙션 | 📋 |
| **Thymeleaf** | 서버사이드 렌더링 | 📋 |
| **Chart.js** | 데이터 시각화 | 📋 |
| **Bootstrap** | UI 프레임워크 | 📋 |

---

## 🏗️ **Phase 1: 개발 환경 및 기반 구축**

### 1.1 개발 환경 설정
- [ ] **Docker Desktop 설치 및 실행 확인**
  - [ ] Docker Desktop 설치
  - [ ] Docker 명령어 실행 테스트
  - [ ] Docker Compose 설치 확인
  
- [ ] **Java 17+ 설치 확인**
  - [ ] JAVA_HOME 환경변수 설정
  - [ ] java -version 확인
  - [ ] javac -version 확인
  
- [ ] **IDE 설정**
  - [ ] IntelliJ IDEA 또는 VS Code 설치
  - [ ] Spring Boot 플러그인 설치
  - [ ] Lombok 플러그인 설치
  - [ ] Git 플러그인 설정
  
- [ ] **Git 초기화 및 .gitignore 설정**
  - [ ] git init 실행
  - [ ] .gitignore 파일 생성 (Spring Boot용)
  - [ ] 첫 커밋 실행

### 1.2 데이터베이스 환경 구축
- [ ] **로컬 개발용 Docker Compose 설정**
  - [ ] docker-compose.dev.yml 작성
    ```yaml
    version: '3.8'
    services:
      mysql:
        image: mysql:8.0
        environment:
          MYSQL_ROOT_PASSWORD: root
          MYSQL_DATABASE: assetmanager
          MYSQL_USER: assetmanager
          MYSQL_PASSWORD: s153244sS!
        ports:
          - "3306:3306"
        volumes:
          - mysql_data:/var/lib/mysql
      
      redis:
        image: redis:7-alpine
        ports:
          - "6379:6379"
      
      phpmyadmin:
        image: phpmyadmin:latest
        environment:
          PMA_HOST: mysql
          PMA_USER: assetmanager
          PMA_PASSWORD: s153244sS!
        ports:
          - "8081:80"
    
    volumes:
      mysql_data:
    ```
  
- [ ] **로컬 테스트 DB 생성**
  - [ ] `docker-compose -f docker-compose.dev.yml up -d` 실행
  - [ ] MySQL 컨테이너 실행 확인
  - [ ] phpMyAdmin 접속 테스트 (http://localhost:8081)
  - [ ] 데이터베이스 연결 확인

### 1.3 Spring Boot 프로젝트 기본 설정
- [ ] **build.gradle 설정**
  ```gradle
  plugins {
      id 'java'
      id 'org.springframework.boot' version '3.3.0'
      id 'io.spring.dependency-management' version '1.1.5'
  }
  
  dependencies {
      implementation 'org.springframework.boot:spring-boot-starter-web'
      implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
      implementation 'org.springframework.boot:spring-boot-starter-security'
      implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
      implementation 'org.springframework.boot:spring-boot-starter-validation'
      implementation 'org.springframework.boot:spring-boot-starter-actuator'
      implementation 'org.springframework.boot:spring-boot-starter-data-redis'
      
      runtimeOnly 'com.mysql:mysql-connector-j'
      
      compileOnly 'org.projectlombok:lombok'
      annotationProcessor 'org.projectlombok:lombok'
      
      testImplementation 'org.springframework.boot:spring-boot-starter-test'
  }
  ```

- [ ] **application.yml 설정**
  ```yaml
  spring:
    profiles:
      active: dev
  
  ---
  # 개발환경
  spring:
    config:
      activate:
        on-profile: dev
    datasource:
      url: jdbc:mysql://localhost:3306/assetmanager
      username: assetmanager
      password: password123
      driver-class-name: com.mysql.cj.jdbc.Driver
    jpa:
      hibernate:
        ddl-auto: create-drop
      show-sql: true
      properties:
        hibernate:
          format_sql: true
    redis:
      host: localhost
      port: 6379
  
  logging:
    level:
      com.assetmanager: DEBUG
      org.springframework.security: DEBUG
  
  management:
    endpoints:
      web:
        exposure:
          include: health,info,metrics
  ```

- [ ] **메인 애플리케이션 클래스**
  ```java
  @SpringBootApplication
  public class AssetManagerApplication {
      public static void main(String[] args) {
          SpringApplication.run(AssetManagerApplication.class, args);
      }
  }
  ```

---

## 🗄️ **Phase 2: 데이터베이스 설계 및 Entity 구현**

### 2.1 DB 스키마 설계

#### **사용자 테이블 (users)**
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NULL,
    name VARCHAR(100) NOT NULL,
    auth_provider ENUM('LOCAL', 'GOOGLE', 'KAKAO', 'NAVER') DEFAULT 'LOCAL',
    role ENUM('USER', 'ADMIN') DEFAULT 'USER',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### **자산 테이블 (assets)**
```sql
CREATE TABLE assets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    symbol VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    asset_type ENUM('CRYPTO', 'STOCK') NOT NULL,
    exchange VARCHAR(50) NOT NULL,
    quantity DECIMAL(18,8) NOT NULL DEFAULT 0,
    average_price DECIMAL(18,2) NOT NULL DEFAULT 0,
    currency VARCHAR(10) DEFAULT 'KRW',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_symbol (user_id, symbol),
    INDEX idx_asset_type (asset_type)
);
```

#### **거래 내역 테이블 (transactions)**
```sql
CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    asset_id BIGINT NOT NULL,
    transaction_type ENUM('BUY', 'SELL') NOT NULL,
    quantity DECIMAL(18,8) NOT NULL,
    price DECIMAL(18,2) NOT NULL,
    total_amount DECIMAL(18,2) NOT NULL,
    fee DECIMAL(18,2) DEFAULT 0,
    transaction_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (asset_id) REFERENCES assets(id) ON DELETE CASCADE,
    INDEX idx_user_date (user_id, transaction_date),
    INDEX idx_asset_date (asset_id, transaction_date)
);
```

#### **가격 히스토리 테이블 (price_history)**
```sql
CREATE TABLE price_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    symbol VARCHAR(50) NOT NULL,
    price DECIMAL(18,2) NOT NULL,
    volume DECIMAL(18,8) DEFAULT 0,
    source VARCHAR(20) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_symbol_timestamp (symbol, timestamp),
    INDEX idx_source_timestamp (source, timestamp)
);
```

### 2.2 JPA Entity 클래스 구현

- [ ] **BaseEntity 클래스**
  ```java
  @MappedSuperclass
  @EntityListeners(AuditingEntityListener.class)
  @Getter
  public abstract class BaseEntity {
      @CreatedDate
      @Column(updatable = false)
      private LocalDateTime createdAt;
      
      @LastModifiedDate
      private LocalDateTime updatedAt;
  }
  ```

- [ ] **User Entity**
  ```java
  @Entity
  @Table(name = "users")
  @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
  public class User extends BaseEntity {
      @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
      private Long id;
      
      @Column(unique = true, nullable = false)
      @Email
      private String email;
      
      private String password;
      
      @Column(nullable = false, length = 100)
      private String name;
      
      @Enumerated(EnumType.STRING)
      @Column(name = "auth_provider")
      private AuthProvider authProvider = AuthProvider.LOCAL;
      
      @Enumerated(EnumType.STRING)
      private Role role = Role.USER;
      
      @Column(name = "is_active")
      private Boolean isActive = true;
      
      @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
      private List<Asset> assets = new ArrayList<>();
      
      public enum AuthProvider {
          LOCAL, GOOGLE, KAKAO, NAVER
      }
      
      public enum Role {
          USER, ADMIN
      }
  }
  ```

- [ ] **Asset Entity**
  ```java
  @Entity
  @Table(name = "assets")
  @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
  public class Asset extends BaseEntity {
      @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
      private Long id;
      
      @ManyToOne(fetch = FetchType.LAZY)
      @JoinColumn(name = "user_id", nullable = false)
      private User user;
      
      @Column(nullable = false, length = 50)
      private String symbol;
      
      @Column(nullable = false, length = 100)
      private String name;
      
      @Enumerated(EnumType.STRING)
      @Column(name = "asset_type")
      private AssetType assetType;
      
      @Column(nullable = false, length = 50)
      private String exchange;
      
      @Column(precision = 18, scale = 8)
      private BigDecimal quantity = BigDecimal.ZERO;
      
      @Column(name = "average_price", precision = 18, scale = 2)
      private BigDecimal averagePrice = BigDecimal.ZERO;
      
      @Column(length = 10)
      private String currency = "KRW";
      
      @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
      private List<Transaction> transactions = new ArrayList<>();
      
      public enum AssetType {
          CRYPTO, STOCK
      }
  }
  ```

### 2.3 Repository 계층 구현

- [ ] **UserRepository**
  ```java
  @Repository
  public interface UserRepository extends JpaRepository<User, Long> {
      Optional<User> findByEmail(String email);
      boolean existsByEmail(String email);
      Optional<User> findByEmailAndAuthProvider(String email, User.AuthProvider provider);
      List<User> findByAuthProvider(User.AuthProvider provider);
      
      @Query("SELECT u FROM User u WHERE u.isActive = true")
      List<User> findActiveUsers();
  }
  ```

- [ ] **AssetRepository**
  ```java
  @Repository
  public interface AssetRepository extends JpaRepository<Asset, Long> {
      List<Asset> findByUserId(Long userId);
      Optional<Asset> findByUserIdAndSymbol(Long userId, String symbol);
      List<Asset> findByUserIdAndAssetType(Long userId, Asset.AssetType assetType);
      
      @Query("SELECT a FROM Asset a WHERE a.user.id = :userId AND a.quantity > 0")
      List<Asset> findActiveAssetsByUserId(@Param("userId") Long userId);
      
      @Query("SELECT SUM(a.quantity * a.averagePrice) FROM Asset a WHERE a.user.id = :userId")
      BigDecimal getTotalInvestmentByUserId(@Param("userId") Long userId);
  }
  ```

---

## 🔐 **Phase 3: 인증 및 보안 시스템**

### 3.1 Spring Security 설정

- [ ] **SecurityConfig 클래스**
  ```java
  @Configuration
  @EnableWebSecurity
  @EnableMethodSecurity
  public class SecurityConfig {
      
      @Bean
      public PasswordEncoder passwordEncoder() {
          return new BCryptPasswordEncoder();
      }
      
      @Bean
      public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
          http
              .csrf(csrf -> csrf.disable())
              .sessionManagement(session -> 
                  session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
              .authorizeHttpRequests(auth -> auth
                  .requestMatchers("/", "/login", "/signup", "/css/**", "/js/**").permitAll()
                  .requestMatchers("/api/auth/**").permitAll()
                  .requestMatchers("/actuator/health").permitAll()
                  .anyRequest().authenticated())
              .oauth2Login(oauth2 -> oauth2
                  .loginPage("/login")
                  .defaultSuccessUrl("/dashboard"))
              .logout(logout -> logout
                  .logoutUrl("/logout")
                  .logoutSuccessUrl("/"));
          
          return http.build();
      }
  }
  ```

### 3.2 사용자 인증 기능

- [ ] **회원가입 (UserService)**
  - [ ] 이메일 중복 검사
  - [ ] 비밀번호 암호화 (BCrypt)
  - [ ] 유효성 검증 (이메일 형식, 비밀번호 강도)
  - [ ] 회원가입 완료 처리

- [ ] **로그인 (AuthService)**
  - [ ] 이메일/비밀번호 인증
  - [ ] JWT 토큰 발급
  - [ ] 로그인 실패 처리
  - [ ] 계정 활성화 상태 확인

- [ ] **로그아웃**
  - [ ] JWT 토큰 블랙리스트 처리 (Redis)
  - [ ] 세션 무효화

### 3.3 사용자 계정 관리

- [ ] **프로필 조회**
  - [ ] 현재 사용자 정보 반환
  - [ ] 포트폴리오 요약 정보 포함

- [ ] **프로필 수정**
  - [ ] 이름 변경
  - [ ] 이메일 변경 (인증 필요)

- [ ] **비밀번호 변경**
  - [ ] 현재 비밀번호 확인
  - [ ] 새 비밀번호 유효성 검사
  - [ ] BCrypt 암호화 후 저장

- [ ] **계정 탈퇴**
  - [ ] 비밀번호 재확인
  - [ ] 관련 데이터 소프트 삭제 (is_active = false)

---

## 📋 **현재 상태 및 다음 단계**

### ✅ **완료 예정 (Phase 1-3)**
- 개발 환경 구축
- 데이터베이스 설계
- 기본 Entity 및 Repository
- Spring Security 기본 설정
- 사용자 인증 시스템

### 🔄 **다음 파일**
- **project_plan_features.md**: Phase 4-7 (자산관리, 포트폴리오분석, API연동, 프론트엔드)
- **project_plan_deploy.md**: Phase 8-10 (고급기능, 배포, 테스트) + 전체 로드맵

---
*프로젝트 재시작: 2025-06-19*
*다음 업데이트: Phase 1 완료 후*
