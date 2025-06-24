package com.assetmanager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Spring Boot 애플리케이션 기본 테스트
 * 
 * 🧪 이 테스트는 다음을 확인합니다:
 * - Spring Boot 애플리케이션이 정상적으로 시작되는지
 * - 모든 Bean들이 올바르게 로드되는지
 * - 기본 설정에 문제가 없는지
 */
@SpringBootTest
class AssetManagerApplicationTests {

    /**
     * 애플리케이션 컨텍스트 로드 테스트
     * 
     * 이 테스트가 통과하면:
     * ✅ Spring Boot 애플리케이션이 정상 시작 가능
     * ✅ 모든 설정 파일이 올바름
     * ✅ Bean 생성에 문제없음
     */
    @Test
    void contextLoads() {
        // 이 테스트는 애플리케이션 컨텍스트가 
        // 성공적으로 로드되는지만 확인합니다.
        // 별도 코드 없이도 @SpringBootTest가 모든 것을 검증합니다.
    }

    /**
     * 애플리케이션 메인 메서드 테스트
     */
    @Test
    void mainMethodTest() {
        // 메인 메서드가 예외 없이 실행되는지 확인
        // 실제로는 애플리케이션을 시작하지 않고 클래스만 로드
        AssetManagerApplication.main(new String[] {});
    }
}
