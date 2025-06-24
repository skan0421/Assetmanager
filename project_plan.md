# 📊 포트폴리오 통합 관리 시스템 v2.0 - 개발 계획

## 📁 **프로젝트 구조**

```
D:/jwj/Assetmanager_jwj/
├── 📋 프로젝트 설정
│   ├── build.gradle                 # Gradle 빌드 설정
│   ├── settings.gradle              # Gradle 프로젝트 설정
│   ├── gradlew.bat                  # Gradle Wrapper (Windows)
│   ├── docker-compose.dev.yml       # 개발환경 Docker 설정
│   ├── schema.sql                   # 데이터베이스 스키마
│   └── .gitignore                   # Git 제외 파일 설정
│
├── 📝 문서화
│   ├── project_plan.md              # 메인 프로젝트 계획
│   ├── project_plan_features.md     # Phase 4-7 상세 계획
│   ├── project_plan_deploy.md       # Phase 8-10 배포 계획
│   ├── domain-models.md             # Domain 모델 상세 설계
│   ├── database-schema.md           # 데이터베이스 스키마 상세
│   └── api-test.http                # API 테스트 파일
│
├── 🗂️ 소스 코드
│   ├── src/main/java/com/assetmanager/
│   │   ├── AssetManagerApplication.java    # 메인 애플리케이션
│   │   ├── domain/                         # Domain 모델 클래스
│   │   ├── mapper/                         # MyBatis Mapper 인터페이스
│   │   ├── service/                        # 비즈니스 로직 서비스
│   │   ├── controller/                     # REST API 컨트롤러
│   │   ├── config/                         # Spring 설정 클래스
│   │   ├── security/                       # Spring Security 설정
│   │   ├── dto/                           # 데이터 전송 객체
│   │   └── test/                          # 테스트 관련 코드 (분리)
│   │       ├── controller/                  # 테스트 컨트롤러
│   │       ├── util/                        # 테스트 유틸리티
│   │       └── config/                      # 테스트 설정
│   │
│   ├── src/main/resources/
│   │   ├── application.yml               # Spring Boot 설정
│   │   ├── mybatis-config.xml            # MyBatis 설정
│   │   ├── mapper/                       # MyBatis XML 매퍼
│   │   └── data.sql                      # 초기 테스트 데이터
│
├── 🐳 Docker 환경
│   ├── MySQL 8.0                     # 메인 데이터베이스 (포트 3306)
│   ├── Redis 7.0                     # 캐싱 서버 (포트 6379)
│   └── phpMyAdmin                    # 데이터베이스 관리 도구 (포트 8081)
│
└── 🎯 현재 상태: Phase 2.1 완료, Phase 2.2 Domain 모델 구현 시작
    📋 다음: User, Asset, Transaction, PriceHistory 도메인 클래스 구현
```

## 🛠️ **기술 스택**

### Backend
| 기술 | 버전 | 목적 | 상태 |
|------|------|------|------|
| **Java** | 17 | 메인 개발 언어 | ✅ |
| **Spring Boot** | 3.3.0 | 웹 프레임워크 | 📋 |
| **MyBatis** | 3.0.3 | SQL 매퍼 프레임워크 | 📋 |
| **MyBatis Spring Boot Starter** | 3.0.3 | Spring Boot 통합 | 📋 |
| **PageHelper** | 1.4.7 | 페이징 처리 | 📋 |
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

## 🗂️ **상세 설계 문서**

### 📄 데이터베이스 설계
- **[database-schema.md](./database-schema.md)** - 데이터베이스 스키마 상세 설계
  - 6개 테이블 설계 (users, assets, transactions, price_history 등)
  - 인덱스 및 외래키 제약조건
  - 성능 최적화 고려사항

### 🏗️ Domain 모델 설계
- **[domain-models.md](./domain-models.md)** - Domain 모델 상세 설계
  - POJO 기반 Domain 클래스들
  - 비즈니스 로직 메서드
  - MyBatis Mapper 인터페이스

### 🗺️ Mapper 구현 계획
- **[mapper-implementation-plan.md](./mapper-implementation-plan.md)** - MyBatis Mapper 구현 계획 ✨ **NEW**
  - 6개 Mapper 상세 구현 계획
  - 우선순위 및 구현 가이드라인
  - 성능 및 보안 고려사항

---

## 🚀 **개발 Phase 순서**

### **🏗️ Phase 1: 프로젝트 기반 구축** ✅
- [x] 개발 환경 설정 (Java 17, Docker, Git)
- [x] Docker 환경 구축 (MySQL, Redis, phpMyAdmin)
- [x] Spring Boot 프로젝트 생성 준비

### **🗄️ Phase 2: MyBatis 기반 데이터 계층** 🔄
- [x] **2.1 프로젝트 구조 생성**
  - [x] build.gradle (MyBatis 의존성)
  - [x] application.yml (MyBatis 설정)
  - [x] mybatis-config.xml 
  - [x] 기본 패키지 구조 생성

- [x] **2.2 Domain 모델 구현**
  - [x] User, Asset, Transaction, PriceHistory 도메인 클래스
  - [x] ApiKey, PortfolioSnapshot 도메인 클래스
  - [x] 비즈니스 로직 메서드 구현
  - [x] AuthProvider, Role, AssetType 등 Enum 클래스

- [🔄] **2.3 MyBatis Mapper 구현**
  - [x] 테스트 구조 정리 및 패키지 구조 수정
  - [x] Mapper 구현 계획 수립 → **[mapper-implementation-plan.md](./mapper-implementation-plan.md)**
  - [x] **2.3.1**: UserMapper, AssetMapper, TransactionMapper 구현
  - [x] **2.3.2**: PriceHistoryMapper, ApiKeyMapper 구현
  - [x] **2.3.3**: PortfolioSnapshotMapper 구현

- [ ] **2.4 데이터베이스 테스트**
  - [ ] Docker MySQL 연결 테스트
  - [ ] 각 Mapper별 기본 CRUD 동작 확인
  - [ ] 비즈니스 쿼리 테스트

### **🔐 Phase 3: 인증 및 보안 시스템** 📋
- [ ] Spring Security + JWT 구현
- [ ] 회원가입/로그인 API
- [ ] 소셜 로그인 연동 (Google, Kakao)
- [ ] 인증 미들웨어 구현

### **📈 Phase 4-7: 핵심 기능 개발** 📋
- 자세한 내용은 **[project_plan_features.md](./project_plan_features.md)** 참고
- Phase 4: 자산 관리 시스템
- Phase 5: 포트폴리오 분석 기능
- Phase 6: 외부 API 연동
- Phase 7: 프론트엔드 개발

### **🚀 Phase 8-10: 배포 및 고도화** 📋
- 자세한 내용은 **[project_plan_deploy.md](./project_plan_deploy.md)** 참고
- Phase 8: 고급 기능 구현
- Phase 9: 배포 및 운영
- Phase 10: 테스트 및 최적화

---

## 🔗 **중요 URL**
- **애플리케이션**: http://localhost:8080
- **phpMyAdmin**: http://localhost:8081 (assetmanager/password123)
- **헬스 체크**: http://localhost:8080/actuator/health

## 🛠️ **개발 명령어**
```bash
# 애플리케이션 실행
.\gradlew.bat bootRun

# 테스트 실행
.\gradlew.bat test

# Docker 컨테이너 시작
docker-compose -f docker-compose.dev.yml up -d

# 포트 확인 및 종료
netstat -ano | findstr :8080
taskkill /PID [PID번호] /F
```

## 🌿 Git 브랜치 전략 (GitHub Flow)

### **브랜치 구조**
```
main                        # 완료된 Phase들만 머지 (프로덕션)
├── develop                 # 현재 개발중인 통합 브랜치
├── feature/phase-2-mybatis # Phase 2: MyBatis 기반 데이터 계층
├── feature/phase-3-auth    # Phase 3: 인증 시스템  
├── feature/phase-4-api     # Phase 4: API 연동
└── feature/frontend        # 프론트엔드 개발
```

### **커밋 메시지 규칙**
- `feat:` 새 기능 추가
- `fix:` 버그 수정
- `docs:` 문서 수정
- `style:` 코드 포맷팅
- `refactor:` 코드 리팩토링
- `test:` 테스트 코드
- `chore:` 빌드 관련 수정

---

## 📋 **현재 상태 및 다음 단계**

### ✅ **완료된 작업**
- MyBatis 기반 아키텍처 설계
- 데이터베이스 스키마 설계 (6개 테이블) → **[database-schema.md](./database-schema.md)**
- Domain 모델 설계 (6개 도메인 + 비즈니스 로직) → **[domain-models.md](./domain-models.md)**
- 프로젝트 구조 계획
- 기술 스택 선정
- 문서 구조 정리 및 분리
- **Phase 2.1 프로젝트 구조 생성 완료**
  - build.gradle MyBatis 의존성 교체
  - application.yml 및 mybatis-config.xml 설정
  - 기본 패키지 구조 확인
- **Phase 2.2 Domain 모델 구현 완료**
  - 모든 Domain 클래스 database-schema.md에 맞춰 수정
  - 누락된 필드들 추가 (createdAt, updatedAt, profileImageUrl 등)
  - User, Asset, Transaction, PriceHistory, ApiKey, PortfolioSnapshot 완료
- **Phase 2.3 MyBatis Mapper 테스트 완료** ✅
  - 테스트 환경 구축 (H2 인메모리 DB, application-test.yml)
  - UserMapper 완전 구현 및 테스트 성공 (15개 테스트 모두 통과)
  - UserMapperTest 생성 완료 및 버그 수정
  - profile_image_url, created_at, updated_at 필드 처리 문제 해결
  - Spring 컨텍스트 로딩 문제 해결 (MyBatis 설정 충돌 해결)

### 🔄 **다음 작업 (Phase 2.3 계속)**
1. **나머지 MyBatis Mapper 테스트 구현** (현재 브랜치: feature/phase-2.4-database-test)
   - ✅ UserMapperTest 완료 (15개 테스트 모두 통과)
   - ✅ AssetMapperTest 완료 (18개 테스트 모두 통과)
   - ✅ TransactionMapperTest 완료 (13개 테스트 중 12개 통과, 1개 실패)
   - 📋 PriceHistoryMapperTest 생성 및 테스트
   - 📋 ApiKeyMapperTest 생성 및 테스트
   - 📋 PortfolioSnapshotMapperTest 생성 및 테스트

2. **Phase 2.4 데이터베이스 연결 테스트**
   - Docker MySQL과의 실제 연결 테스트
   - 모든 Mapper의 기본 CRUD 동작 확인
   - 비즈니스 쿼리 성능 테스트

3. **참고 문서**
   - mapper-implementation-plan.md에 상세한 구현 계획

---

## 🎯 프로젝트 개요
- **프로젝트명**: Asset Manager - 포트폴리오 통합 관리 시스템 (PMS v2.0)
- **버전**: v2.0.0 (MyBatis 기반 재구축)
- **개발 시작**: 2025-06-22
- **개발 기간**: 2025.06 ~ 2025.08 (3개월)
- **프로젝트 타입**: 웹 애플리케이션

### **MyBatis 선택 이유**
1. **SQL 완전 제어**: 복잡한 포트폴리오 계산 쿼리 최적화 가능
2. **성능 최적화**: 대용량 가격 데이터 처리에 유리
3. **학습 용이성**: SQL 기반으로 이해하기 쉬움
4. **유연성**: 다양한 집계 쿼리 작성 가능

---

*프로젝트 계획서 업데이트: 2025-06-22*
*다음 업데이트: Phase 2.1 Spring Boot 프로젝트 생성 후*
