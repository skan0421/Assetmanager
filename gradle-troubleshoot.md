# Gradle 빌드 문제 해결 스크립트

## 문제: Spring Boot 플러그인을 찾을 수 없음

### 해결 순서:

1. **캐시 정리**
```bash
cd D:\jwj\Assetmanager_jwj
gradle clean
rmdir /s /q .gradle
```

2. **인터넷 연결 확인**
```bash
ping repo1.maven.org
ping plugins.gradle.org
```

3. **Gradle 재시도**
```bash
gradle build --refresh-dependencies
```

4. **IDE에서 Gradle 새로고침**
- IntelliJ: Gradle 탭 → 새로고침 버튼
- Eclipse: 프로젝트 우클릭 → Gradle → Refresh

### 대안 버전 (문제 지속시):

build.gradle의 Spring Boot 버전을 변경:
```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.5'  // 3.3.0 → 3.2.5
    id 'io.spring.dependency-management' version '1.1.4'
}
```

### 오프라인 환경인 경우:
```bash
gradle build --offline
```
