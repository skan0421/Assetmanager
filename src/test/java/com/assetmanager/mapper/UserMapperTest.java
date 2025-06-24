package com.assetmanager.mapper;

import com.assetmanager.domain.User;
import com.assetmanager.domain.Role;
import com.assetmanager.domain.AuthProvider;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * UserMapper 테스트 클래스 (database-schema.md 기반)
 * Phase 2.3: MyBatis Mapper 구현 - UserMapper 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .password("encoded_password123")
                .name("Test User")
                .authProvider(AuthProvider.LOCAL)
                .role(Role.USER)
                .isActive(true)
                .profileImageUrl("https://example.com/profile.jpg")
                .lastLoginAt(LocalDateTime.now().minusDays(1))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // =================
    // 기본 CRUD 테스트
    // =================

    @Test
    @Order(1)
    @DisplayName("사용자 등록 테스트")
    void testInsert() {
        // When
        userMapper.insert(testUser);

        // Then
        assertThat(testUser.getId()).isNotNull();
        assertThat(testUser.getId()).isGreaterThan(0L);
    }

    @Test
    @Order(2)
    @DisplayName("ID로 사용자 조회 테스트")
    void testFindById() {
        // Given
        userMapper.insert(testUser);
        Long userId = testUser.getId();

        // When
        Optional<User> found = userMapper.findById(userId);

        // Then
        assertThat(found).isPresent();
        User user = found.get();
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getName()).isEqualTo("Test User");
        assertThat(user.getAuthProvider()).isEqualTo(AuthProvider.LOCAL);
        assertThat(user.getRole()).isEqualTo(Role.USER);
        assertThat(user.getIsActive()).isTrue();
        assertThat(user.getProfileImageUrl()).isEqualTo("https://example.com/profile.jpg");
    }

    @Test
    @Order(3)
    @DisplayName("이메일로 사용자 조회 테스트")
    void testFindByEmail() {
        // Given
        userMapper.insert(testUser);

        // When
        Optional<User> found = userMapper.findByEmail("test@example.com");

        // Then
        assertThat(found).isPresent();
        User user = found.get();
        assertThat(user.getName()).isEqualTo("Test User");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @Order(4)
    @DisplayName("존재하지 않는 이메일 조회 테스트")
    void testFindByEmailNotFound() {
        // When
        Optional<User> found = userMapper.findByEmail("notfound@example.com");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @Order(5)
    @DisplayName("사용자 정보 수정 테스트")
    void testUpdate() {
        // Given
        userMapper.insert(testUser);
        Long userId = testUser.getId();

        // When
        testUser.setName("Updated User");
        testUser.setProfileImageUrl("https://example.com/new_profile.jpg");
        testUser.setUpdatedAt(LocalDateTime.now());
        userMapper.update(testUser);

        // Then
        Optional<User> updated = userMapper.findById(userId);
        assertThat(updated).isPresent();
        User user = updated.get();
        assertThat(user.getName()).isEqualTo("Updated User");
        assertThat(user.getProfileImageUrl()).isEqualTo("https://example.com/new_profile.jpg");
    }

    @Test
    @Order(6)
    @DisplayName("사용자 소프트 삭제 테스트")
    void testSoftDelete() {
        // Given
        userMapper.insert(testUser);
        Long userId = testUser.getId();

        // When
        userMapper.softDelete(userId);

        // Then
        Optional<User> deleted = userMapper.findById(userId);
        assertThat(deleted).isPresent();
        assertThat(deleted.get().getIsActive()).isFalse();
    }

    // =================
    // 인증 관련 테스트
    // =================

    @Test
    @Order(7)
    @DisplayName("마지막 로그인 시간 업데이트 테스트")
    void testUpdateLastLogin() {
        // Given
        userMapper.insert(testUser);
        Long userId = testUser.getId();
        LocalDateTime loginTime = LocalDateTime.now();

        // When
        userMapper.updateLastLogin(userId, loginTime);

        // Then
        Optional<User> updated = userMapper.findById(userId);
        assertThat(updated).isPresent();
        User user = updated.get();
        assertThat(user.getLastLoginAt()).isEqualToIgnoringNanos(loginTime);
    }

    @Test
    @Order(8)
    @DisplayName("인증 제공자별 사용자 조회 테스트")
    void testFindByAuthProvider() {
        // Given
        // LOCAL 사용자
        userMapper.insert(testUser);
        
        // GOOGLE 사용자
        User googleUser = User.builder()
                .email("google@example.com")
                .name("Google User")
                .authProvider(AuthProvider.GOOGLE)
                .role(Role.USER)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        userMapper.insert(googleUser);

        // When
        List<User> localUsers = userMapper.findByAuthProvider(AuthProvider.LOCAL);
        List<User> googleUsers = userMapper.findByAuthProvider(AuthProvider.GOOGLE);

        // Then
        assertThat(localUsers).hasSize(1);
        assertThat(localUsers.get(0).getEmail()).isEqualTo("test@example.com");

        assertThat(googleUsers).hasSize(1);
        assertThat(googleUsers.get(0).getEmail()).isEqualTo("google@example.com");
    }

    // =================
    // 관리 기능 테스트
    // =================

    @Test
    @Order(9)
    @DisplayName("활성 사용자 조회 테스트")
    void testFindActiveUsers() {
        // Given
        userMapper.insert(testUser); // 활성 사용자
        
        User inactiveUser = User.builder()
                .email("inactive@example.com")
                .name("Inactive User")
                .authProvider(AuthProvider.LOCAL)
                .role(Role.USER)
                .isActive(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        userMapper.insert(inactiveUser); // 비활성 사용자

        // When
        List<User> activeUsers = userMapper.findActiveUsers();

        // Then
        assertThat(activeUsers).hasSize(1);
        assertThat(activeUsers.get(0).getEmail()).isEqualTo("test@example.com");
        assertThat(activeUsers.get(0).getIsActive()).isTrue();
    }

    @Test
    @Order(10)
    @DisplayName("활성 사용자 수 조회 테스트")
    void testCountActiveUsers() {
        // Given
        userMapper.insert(testUser); // 활성 사용자
        
        User inactiveUser = User.builder()
                .email("inactive@example.com")
                .name("Inactive User")
                .authProvider(AuthProvider.LOCAL)
                .role(Role.USER)
                .isActive(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        userMapper.insert(inactiveUser); // 비활성 사용자

        // When
        int activeCount = userMapper.countActiveUsers();

        // Then
        assertThat(activeCount).isEqualTo(1);
    }

    @Test
    @Order(11)
    @DisplayName("권한별 사용자 조회 테스트")
    void testFindByRole() {
        // Given
        userMapper.insert(testUser); // USER 권한
        
        User adminUser = User.builder()
                .email("admin@example.com")
                .name("Admin User")
                .authProvider(AuthProvider.LOCAL)
                .role(Role.ADMIN)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        userMapper.insert(adminUser); // ADMIN 권한

        // When
        List<User> regularUsers = userMapper.findByRole(Role.USER);
        List<User> adminUsers = userMapper.findByRole(Role.ADMIN);

        // Then
        assertThat(regularUsers).hasSize(1);
        assertThat(regularUsers.get(0).getRole()).isEqualTo(Role.USER);

        assertThat(adminUsers).hasSize(1);
        assertThat(adminUsers.get(0).getRole()).isEqualTo(Role.ADMIN);
    }

    // =================
    // 소셜 로그인 테스트
    // =================

    @Test
    @Order(12)
    @DisplayName("소셜 로그인 사용자 조회 테스트")
    void testFindSocialUsers() {
        // Given
        userMapper.insert(testUser); // LOCAL 사용자
        
        User kakaoUser = User.builder()
                .email("kakao@example.com")
                .name("Kakao User")
                .authProvider(AuthProvider.KAKAO)
                .role(Role.USER)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        userMapper.insert(kakaoUser);

        User naverUser = User.builder()
                .email("naver@example.com")
                .name("Naver User")
                .authProvider(AuthProvider.NAVER)
                .role(Role.USER)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        userMapper.insert(naverUser);

        // When
        List<User> socialUsers = userMapper.findSocialUsers();

        // Then
        assertThat(socialUsers).hasSize(2);
        assertThat(socialUsers).extracting(User::getAuthProvider)
                .containsExactlyInAnyOrder(AuthProvider.KAKAO, AuthProvider.NAVER);
    }

    @Test
    @Order(13)
    @DisplayName("최근 가입 사용자 조회 테스트")
    void testFindRecentUsers() {
        // Given
        LocalDateTime baseTime = LocalDateTime.now();
        
        // 1시간 전 가입 사용자
        User recentUser = User.builder()
                .email("recent@example.com")
                .name("Recent User")
                .authProvider(AuthProvider.LOCAL)
                .role(Role.USER)
                .isActive(true)
                .createdAt(baseTime.minusHours(1))
                .updatedAt(baseTime.minusHours(1))
                .build();
        userMapper.insert(recentUser);

        // 10일 전 가입 사용자
        User oldUser = User.builder()
                .email("old@example.com")
                .name("Old User")
                .authProvider(AuthProvider.LOCAL)
                .role(Role.USER)
                .isActive(true)
                .createdAt(baseTime.minusDays(10))
                .updatedAt(baseTime.minusDays(10))
                .build();
        userMapper.insert(oldUser);

        // When
        List<User> recentUsers = userMapper.findRecentUsers(5); // 최근 5명

        // Then
        assertThat(recentUsers).hasSize(2);
        assertThat(recentUsers.get(0).getEmail()).isEqualTo("recent@example.com"); // 최신순
    }

    @Test
    @Order(14)
    @DisplayName("빈 결과 조회 테스트")
    void testEmptyResults() {
        // When
        List<User> activeUsers = userMapper.findActiveUsers();
        int activeCount = userMapper.countActiveUsers();
        List<User> socialUsers = userMapper.findSocialUsers();

        // Then
        assertThat(activeUsers).isEmpty();
        assertThat(activeCount).isEqualTo(0);
        assertThat(socialUsers).isEmpty();
    }

    @Test
    @Order(15)
    @DisplayName("중복 이메일 처리 테스트")
    void testDuplicateEmail() {
        // Given
        userMapper.insert(testUser);

        User duplicateUser = User.builder()
                .email("test@example.com") // 동일한 이메일
                .name("Duplicate User")
                .authProvider(AuthProvider.LOCAL)
                .role(Role.USER)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // When & Then
        assertThatThrownBy(() -> userMapper.insert(duplicateUser))
                .isInstanceOf(Exception.class); // 데이터베이스 제약조건 위반
    }
}