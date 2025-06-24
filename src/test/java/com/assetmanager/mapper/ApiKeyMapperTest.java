package com.assetmanager.mapper;

import com.assetmanager.domain.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApiKeyMapperTest {

    @Autowired
    private ApiKeyMapper apiKeyMapper;

    @Autowired
    private UserMapper userMapper;

    private User user;
    private ApiKey apiKey;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("api.user@example.com")
                .password("encoded_password")
                .name("API User")
                .authProvider(AuthProvider.LOCAL)
                .role(Role.USER)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        userMapper.insert(user);

        apiKey = ApiKey.builder()
                .userId(user.getId())
                .exchangeType(ExchangeType.UPBIT)
                .exchangeName("Upbit")
                .accessKey("access")
                .secretKey("secret")
                .apiPermissions("read")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @Order(1)
    @DisplayName("API 키 등록 및 조회 테스트")
    void testInsertAndFind() {
        apiKeyMapper.insert(apiKey);
        assertThat(apiKey.getId()).isNotNull();

        Optional<ApiKey> found = apiKeyMapper.findById(apiKey.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getExchangeName()).isEqualTo("Upbit");
    }

    @Test
    @Order(2)
    @DisplayName("API 키 수정 테스트")
    void testUpdate() {
        apiKeyMapper.insert(apiKey);
        Long id = apiKey.getId();

        apiKey.setExchangeName("UpbitKR");
        apiKey.setApiPermissions("read,trade");
        apiKeyMapper.update(apiKey);

        Optional<ApiKey> updated = apiKeyMapper.findById(id);
        assertThat(updated).isPresent();
        assertThat(updated.get().getExchangeName()).isEqualTo("UpbitKR");
        assertThat(updated.get().getApiPermissions()).isEqualTo("read,trade");
    }

    @Test
    @Order(3)
    @DisplayName("API 키 비활성화 테스트")
    void testDeactivate() {
        apiKeyMapper.insert(apiKey);
        Long id = apiKey.getId();

        apiKeyMapper.deactivate(id);
        Optional<ApiKey> deactivated = apiKeyMapper.findById(id);
        assertThat(deactivated).isPresent();
        assertThat(deactivated.get().getIsActive()).isFalse();
    }
}
