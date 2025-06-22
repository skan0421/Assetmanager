# 📊 포트폴리오 관리 시스템 - 기능 구현 계획 (Phase 4-7) - MyBatis 기반

> **주요 변경**: JPA → MyBatis 전환으로 인한 기능 구현 방식 변경

---

## 💰 **Phase 4: 자산 관리 기능**

### 4.1 자산 CRUD 기능

- [ ] **자산 추가 (AssetService)**
  - [ ] 종목 검색 기능 구현
    ```java
    public List<AssetSearchDto> searchAssets(String keyword, AssetType type) {
        // 외부 API에서 종목 검색
        // 업비트: 암호화폐 목록
        // 키움/야후: 주식 목록
    }
    ```
  - [ ] 수동 입력 폼 검증
  - [ ] 거래소별 종목 코드 매핑
  - [ ] 중복 자산 체크 (동일 사용자, 동일 종목)
  - [ ] 유효성 검증 (양수 값, 필수 필드)

- [ ] **자산 조회**
  - [ ] 사용자별 보유 자산 목록 API
    ```java
    @GetMapping("/api/assets")
    public ResponseEntity<List<AssetResponseDto>> getUserAssets(
        @AuthenticationPrincipal UserDetails user) {
        // 현재 가격 정보 포함하여 반환
    }
    ```
  - [ ] 자산별 상세 정보 (수익률, 평가금액 포함)
  - [ ] 현재 가격 정보 실시간 조회
  - [ ] 페이징 및 정렬 기능

- [ ] **자산 수정**
  - [ ] 보유량 수정 (추가 매수/매도 반영)
  - [ ] 평균 매수가 자동 재계산
  - [ ] 거래소 변경 (예: 업비트 → 바이낸스)
  - [ ] 수정 이력 로깅

- [ ] **자산 삭제**
  - [ ] 삭제 확인 과정 (2단계 확인)
  - [ ] 관련 거래 내역 처리 옵션
    - 소프트 삭제: is_active = false
    - 하드 삭제: 완전 삭제 + 거래내역 보관
  - [ ] 삭제 전 백업 생성

### 4.2 거래 내역 관리

- [ ] **거래 내역 추가 (TransactionService)**
  ```java
  @Transactional
  public TransactionResponseDto addTransaction(TransactionRequestDto request) {
      // 1. 거래 내역 저장
      // 2. 자산 보유량 업데이트
      // 3. 평균 매수가 재계산
      // 4. 포트폴리오 통계 갱신
  }
  ```
  - [ ] 매수/매도 거래 입력
  - [ ] 자동 평균 매수가 계산 (가중평균)
    ```java
    // 새로운 평균 매수가 = (기존 투자금액 + 신규 투자금액) / (기존 수량 + 신규 수량)
    BigDecimal newAvgPrice = 
        (currentQuantity.multiply(currentAvgPrice).add(newQuantity.multiply(newPrice)))
        .divide(currentQuantity.add(newQuantity), 2, RoundingMode.HALF_UP);
    ```
  - [ ] 보유량 자동 업데이트
  - [ ] 수수료 계산 및 반영

- [ ] **거래 내역 조회**
  - [ ] 전체 거래 내역 목록 (페이징)
  - [ ] 자산별 거래 내역 필터링
  - [ ] 기간별 조회 (일/주/월/년)
  - [ ] 거래 유형별 필터 (매수/매도)
  - [ ] CSV/Excel 내보내기 기능

- [ ] **거래 내역 수정/삭제**
  - [ ] 거래 정보 수정 (가격, 수량, 날짜)
  - [ ] 평균 매수가 재계산 (전체 거래 내역 기반)
  - [ ] 보유량 재계산
  - [ ] 수정 이력 추적

---

## 📊 **Phase 5: 포트폴리오 분석 기능**

### 5.1 포트폴리오 현황 (PortfolioService)

- [ ] **전체 포트폴리오 요약**
  ```java
  public PortfolioSummaryDto getPortfolioSummary(Long userId) {
      // 1. 총 자산 가치 계산 (현재가 기준)
      // 2. 총 투자금액 계산 (매수금액 합계)
      // 3. 총 수익/손실 계산 (평가금액 - 투자금액)
      // 4. 수익률 계산 ((평가금액 - 투자금액) / 투자금액 * 100)
  }
  ```
  - [ ] 총 자산 가치 계산 (실시간 가격 기반)
  - [ ] 총 투자금액 계산
  - [ ] 총 수익/손실 계산
  - [ ] 수익률 계산 (%) 
  - [ ] 일일 변동률 계산

- [ ] **자산별 분석**
  - [ ] 개별 자산 수익/손실
    ```java
    // 평가손익 = (현재가 - 평균매수가) * 보유수량
    BigDecimal unrealizedPnL = currentPrice.subtract(avgPrice).multiply(quantity);
    ```
  - [ ] 자산별 포트폴리오 비중 계산
  - [ ] 평가손익 vs 실현손익 구분
  - [ ] 자산별 수익률 순위

- [ ] **자산 분배 현황**
  - [ ] 주식 vs 암호화폐 비율
  - [ ] 거래소별 자산 분배
  - [ ] 상위 보유 자산 TOP 10
  - [ ] 통화별 분배 (KRW, USD)

### 5.2 성과 분석

- [ ] **수익률 추이 분석**
  - [ ] 일별/주별/월별 포트폴리오 가치 추이
  - [ ] 누적 수익률 계산
  - [ ] 기간별 성과 비교 (1주일, 1개월, 3개월, 1년)
  - [ ] 연환산 수익률 계산

- [ ] **위험 분석 (고급)**
  - [ ] 포트폴리오 변동성 계산 (표준편차)
  - [ ] 최대 낙폭(Maximum Drawdown) 계산
  - [ ] 샤프 비율 계산 (위험 대비 수익률)
  - [ ] 자산별 베타 계수 (시장 대비 변동성)

### 5.3 데이터 시각화

- [ ] **Chart.js 기반 차트 구현**
  - [ ] **자산 분배 파이차트**
    ```javascript
    // 자산별 비중 표시
    const pieChart = new Chart(ctx, {
        type: 'pie',
        data: {
            labels: assetNames,
            datasets: [{
                data: assetValues,
                backgroundColor: colorPalette
            }]
        }
    });
    ```
  
  - [ ] **수익률 추이 라인차트**
    ```javascript
    // 시간별 포트폴리오 가치 변화
    const lineChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: dates,
            datasets: [{
                label: '포트폴리오 가치',
                data: portfolioValues,
                borderColor: 'rgb(75, 192, 192)'
            }]
        }
    });
    ```
  
  - [ ] **자산별 수익 바차트**
  - [ ] **거래량 히스토그램**
  - [ ] **실시간 업데이트 차트**

- [ ] **대시보드 구성**
  - [ ] 실시간 포트폴리오 현황 카드
  - [ ] 주요 지표 위젯 (총 자산, 수익률, 일일 변동)
  - [ ] 최근 거래 내역 요약
  - [ ] 수익률 상위/하위 자산 순위

---

## 🔄 **Phase 6: 외부 API 연동**

### 6.1 암호화폐 API 연동

- [ ] **업비트 API 클라이언트 (UpbitApiClient)**
  ```java
  @Service
  public class UpbitApiClient {
      private final WebClient webClient;
      
      public List<TickerDto> getTickers(List<String> markets) {
          // GET https://api.upbit.com/v1/ticker
      }
      
      public List<CandleDto> getDayCandles(String market, int count) {
          // GET https://api.upbit.com/v1/candles/days
      }
  }
  ```
  - [ ] REST API 클라이언트 구현 (WebClient 사용)
  - [ ] 현재 가격 조회 API
  - [ ] 일봉/분봉 데이터 수집 API
  - [ ] 거래량 정보 조회
  - [ ] API 키 관리 (읽기 전용)
  - [ ] Rate Limiting 처리 (초당 요청 제한)

- [ ] **WebSocket 실시간 데이터**
  ```java
  @Component
  public class UpbitWebSocketClient {
      @EventListener
      public void onTickerReceived(TickerEvent event) {
          // 실시간 가격 업데이트 처리
          priceUpdateService.updatePrice(event.getSymbol(), event.getPrice());
      }
  }
  ```
  - [ ] WebSocket 연결 관리
  - [ ] 실시간 가격 업데이트
  - [ ] 연결 끊김 시 재연결 로직
  - [ ] 사용자별 구독 관리 (사용자가 보유한 자산만)

### 6.2 주식 API 연동

- [ ] **Alpha Vantage API (해외 주식)**
  ```java
  @Service
  public class AlphaVantageApiClient {
      public StockQuoteDto getQuote(String symbol) {
          // GET https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol={symbol}
      }
  }
  ```
  - [ ] 해외 주식 실시간 가격
  - [ ] 주식 기본 정보 조회
  - [ ] 일일 가격 히스토리
  - [ ] API 호출 제한 관리 (무료: 5 calls/min, 500 calls/day)

- [ ] **Yahoo Finance API (대안)**
  - [ ] 글로벌 주식 데이터
  - [ ] ETF, 지수 데이터
  - [ ] 무료 사용 가능

- [ ] **키움증권 Open API+ (국내 주식 - 선택)**
  - [ ] 실시간 한국 주식 가격
  - [ ] KOSPI, KOSDAQ 데이터
  - [ ] API 신청 및 인증 필요

### 6.3 가격 데이터 관리

- [ ] **데이터 수집 스케줄러 (PriceCollectorService)**
  ```java
  @Service
  public class PriceCollectorService {
      
      @Scheduled(fixedRate = 60000) // 1분마다
      public void collectCryptoPrices() {
          // 사용자들이 보유한 암호화폐 가격 수집
      }
      
      @Scheduled(cron = "0 0 9-15 * * MON-FRI") // 주식 시장 시간
      public void collectStockPrices() {
          // 사용자들이 보유한 주식 가격 수집
      }
  }
  ```
  - [ ] @Scheduled 작업 설정
  - [ ] 암호화폐: 1분마다 수집
  - [ ] 주식: 시장 시간에만 수집
  - [ ] 오류 처리 및 재시도 로직
  - [ ] 배치 처리로 성능 최적화

- [ ] **Redis 캐싱 전략**
  ```java
  @Service
  public class PriceCacheService {
      
      @Cacheable(value = "asset-prices", key = "#symbol")
      public BigDecimal getCurrentPrice(String symbol) {
          // 캐시에서 먼저 조회, 없으면 API 호출
      }
      
      @CacheEvict(value = "asset-prices", key = "#symbol")
      public void evictPriceCache(String symbol) {
          // 가격 업데이트 시 캐시 무효화
      }
  }
  ```
  - [ ] 가격 데이터 Redis 캐싱 (TTL: 1분)
  - [ ] 캐시 Hit/Miss 모니터링
  - [ ] 캐시 워밍업 (애플리케이션 시작 시)
  - [ ] 캐시 만료 정책 설정

---

## 🌐 **Phase 7: 웹 프론트엔드**

### 7.1 기본 UI 구조

- [ ] **레이아웃 설계 (Thymeleaf)**
  - [ ] **fragments/layout.html** - 공통 레이아웃
    ```html
    <!DOCTYPE html>
    <html lang="ko" xmlns:th="http://www.thymeleaf.org">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title th:text="'Asset Manager - ' + ${title}">Asset Manager</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
        <link rel="stylesheet" th:href="@{/css/app.css}">
    </head>
    <body>
        <nav th:replace="fragments/navbar :: navbar"></nav>
        <main class="container-fluid">
            <div th:replace="${content}"></div>
        </main>
        <footer th:replace="fragments/footer :: footer"></footer>
    </body>
    </html>
    ```
  
  - [ ] **헤더 (navbar)**
    - 로고 및 브랜드명
    - 네비게이션 메뉴 (대시보드, 자산, 거래내역, 분석)
    - 사용자 드롭다운 (프로필, 설정, 로그아웃)
    - 실시간 포트폴리오 요약 표시
  
  - [ ] **사이드바 (선택)**
    - 빠른 메뉴 접근
    - 보유 자산 요약
    - 최근 거래 내역
  
  - [ ] **푸터**
    - 저작권 정보
    - 버전 정보
    - 지원 링크

- [ ] **반응형 디자인**
  - [ ] Bootstrap 5.3 그리드 시스템 활용
  - [ ] 모바일 우선 설계 (Mobile First)
  - [ ] 태블릿 최적화 (768px-1024px)
  - [ ] 데스크톱 최적화 (1024px+)
  - [ ] 다크모드 지원 (선택)

### 7.2 페이지별 구현

- [ ] **메인 대시보드 (/dashboard)**
  ```html
  <!-- dashboard.html -->
  <div layout:fragment="content">
      <div class="row">
          <div class="col-md-3">
              <div class="card portfolio-summary">
                  <div class="card-body">
                      <h5>총 자산</h5>
                      <h2 id="total-value" th:text="${portfolio.totalValue}">₩0</h2>
                      <span id="daily-change" th:text="${portfolio.dailyChange}">+0.00%</span>
                  </div>
              </div>
          </div>
          <!-- 추가 요약 카드들 -->
      </div>
      
      <div class="row mt-4">
          <div class="col-md-6">
              <canvas id="asset-distribution-chart"></canvas>
          </div>
          <div class="col-md-6">
              <canvas id="performance-chart"></canvas>
          </div>
      </div>
  </div>
  ```
  - [ ] 포트폴리오 요약 카드 (총 자산, 수익률, 일일 변동)
  - [ ] 실시간 차트 표시 (자산 분배, 수익률 추이)
  - [ ] 최근 거래 내역 테이블 (최근 5건)
  - [ ] 빠른 작업 버튼 (자산 추가, 거래 입력)
  - [ ] 알림 메시지 영역

- [ ] **로그인/회원가입 페이지**
  - [ ] **login.html**
    ```html
    <form th:action="@{/api/auth/login}" method="post">
        <div class="mb-3">
            <label for="email" class="form-label">이메일</label>
            <input type="email" class="form-control" id="email" name="email" required>
        </div>
        <div class="mb-3">
            <label for="password" class="form-label">비밀번호</label>
            <input type="password" class="form-control" id="password" name="password" required>
        </div>
        <button type="submit" class="btn btn-primary w-100">로그인</button>
    </form>
    
    <!-- 소셜 로그인 -->
    <div class="social-login mt-3">
        <a href="/oauth2/authorization/google" class="btn btn-outline-danger">Google 로그인</a>
        <a href="/oauth2/authorization/kakao" class="btn btn-outline-warning">Kakao 로그인</a>
    </div>
    ```
  - [ ] 로그인 폼 (이메일/비밀번호)
  - [ ] 회원가입 폼 (이메일, 비밀번호, 이름, 비밀번호 확인)
  - [ ] 소셜 로그인 버튼 (Google, Kakao)
  - [ ] 비밀번호 찾기 링크
  - [ ] 폼 유효성 검사 (JavaScript)

- [ ] **자산 관리 페이지 (/assets)**
  - [ ] 자산 목록 테이블
    ```html
    <table class="table table-striped">
        <thead>
            <tr>
                <th>자산명</th>
                <th>종목코드</th>
                <th>보유량</th>
                <th>평균매수가</th>
                <th>현재가</th>
                <th>평가금액</th>
                <th>수익률</th>
                <th>액션</th>
            </tr>
        </thead>
        <tbody>
            <tr th:each="asset : ${assets}">
                <td th:text="${asset.name}"></td>
                <td th:text="${asset.symbol}"></td>
                <td th:text="${asset.quantity}"></td>
                <td th:text="${asset.averagePrice}"></td>
                <td class="current-price" th:data-symbol="${asset.symbol}">₩0</td>
                <td class="evaluation-amount">₩0</td>
                <td class="profit-rate">0.00%</td>
                <td>
                    <button class="btn btn-sm btn-primary edit-asset" th:data-id="${asset.id}">수정</button>
                    <button class="btn btn-sm btn-danger delete-asset" th:data-id="${asset.id}">삭제</button>
                </td>
            </tr>
        </tbody>
    </table>
    ```
  - [ ] 자산 추가 모달 (종목 검색, 수동 입력)
  - [ ] 자산 수정/삭제 기능
  - [ ] 검색 및 필터링 (자산 타입, 거래소별)
  - [ ] 정렬 기능 (이름, 수익률, 보유량)

- [ ] **거래 내역 페이지 (/transactions)**
  - [ ] 거래 내역 테이블 (페이징)
  - [ ] 거래 추가 모달
  - [ ] 기간별 필터 (1주일, 1개월, 3개월, 1년)
  - [ ] 거래 유형 필터 (매수/매도)
  - [ ] CSV/Excel 다운로드 기능

- [ ] **분석 페이지 (/analytics)**
  - [ ] 수익률 추이 차트 (라인차트)
  - [ ] 자산 분배 차트 (파이차트)
  - [ ] 성과 분석 테이블
  - [ ] 기간별 성과 비교

- [ ] **설정 페이지 (/settings)**
  - [ ] 프로필 수정 (이름, 이메일)
  - [ ] 비밀번호 변경
  - [ ] API 키 관리 (업비트, 키움증권)
  - [ ] 알림 설정 (이메일, 브라우저 push)
  - [ ] 계정 탈퇴

### 7.3 JavaScript 기능

- [ ] **AJAX 통신 (app.js)**
  ```javascript
  class ApiClient {
      constructor() {
          this.baseURL = '/api';
          this.token = localStorage.getItem('jwt-token');
      }
      
      async get(endpoint) {
          const response = await fetch(`${this.baseURL}${endpoint}`, {
              headers: {
                  'Authorization': `Bearer ${this.token}`,
                  'Content-Type': 'application/json'
              }
          });
          return response.json();
      }
      
      async post(endpoint, data) {
          const response = await fetch(`${this.baseURL}${endpoint}`, {
              method: 'POST',
              headers: {
                  'Authorization': `Bearer ${this.token}`,
                  'Content-Type': 'application/json'
              },
              body: JSON.stringify(data)
          });
          return response.json();
      }
  }
  ```
  - [ ] Axios 또는 Fetch API 사용
  - [ ] JWT 토큰 자동 포함
  - [ ] 오류 처리 및 사용자 피드백
  - [ ] 로딩 스피너 표시

- [ ] **실시간 업데이트 (websocket.js)**
  ```javascript
  class WebSocketClient {
      constructor() {
          this.ws = new WebSocket('ws://localhost:8080/ws/prices');
          this.setupEventHandlers();
      }
      
      setupEventHandlers() {
          this.ws.onmessage = (event) => {
              const priceUpdate = JSON.parse(event.data);
              this.updatePriceDisplay(priceUpdate);
          };
      }
      
      updatePriceDisplay(priceUpdate) {
          const elements = document.querySelectorAll(`[data-symbol="${priceUpdate.symbol}"]`);
          elements.forEach(el => {
              el.textContent = this.formatPrice(priceUpdate.price);
              el.classList.add(priceUpdate.change > 0 ? 'price-up' : 'price-down');
          });
      }
  }
  ```
  - [ ] WebSocket 연결 및 관리
  - [ ] 실시간 가격 업데이트
  - [ ] 포트폴리오 가치 실시간 갱신
  - [ ] 알림 메시지 실시간 표시

- [ ] **폼 유효성 검사 (validation.js)**
  - [ ] 클라이언트 사이드 검증
  - [ ] 실시간 피드백 (입력 중 검증)
  - [ ] 서버 응답 오류 처리
  - [ ] 성공/실패 메시지 표시

---

## 📋 **Phase 4-7 완료 체크리스트**

### ✅ **Phase 4: 자산 관리**
- 자산 CRUD API 구현
- 거래 내역 관리 시스템
- 평균 매수가 자동 계산

### ✅ **Phase 5: 포트폴리오 분석**
- 포트폴리오 현황 계산
- 수익률 분석 로직
- Chart.js 데이터 시각화

### ✅ **Phase 6: 외부 API 연동**
- 업비트 API 클라이언트
- 실시간 가격 업데이트
- 데이터 수집 스케줄러

### ✅ **Phase 7: 웹 프론트엔드**
- Thymeleaf 템플릿 구조
- 반응형 대시보드
- 실시간 JavaScript 기능

---
*다음: project_plan_deploy.md (Phase 8-10 + 전체 로드맵)*
