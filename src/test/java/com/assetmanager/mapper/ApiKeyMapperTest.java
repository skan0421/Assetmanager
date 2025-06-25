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

    @Test
    @Order(4)
    @DisplayName("사용자별 API 키 조회 테스트")
    void testFindByUserQueries() {
        apiKeyMapper.insert(apiKey);

        ApiKey second = ApiKey.builder()
                .userId(user.getId())
                .exchangeType(ExchangeType.KIWOOM)
                .exchangeName("Kiwoom")
                .accessKey("a2")
                .secretKey("s2")
                .apiPermissions("read,trade")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        apiKeyMapper.insert(second);

        assertThat(apiKeyMapper.findByUserId(user.getId())).hasSize(2);
        assertThat(apiKeyMapper.findActiveApiKeysByUserId(user.getId())).hasSize(2);

        Optional<ApiKey> byType = apiKeyMapper.findByUserIdAndExchangeType(user.getId(), ExchangeType.UPBIT);
        assertThat(byType).isPresent();

        Optional<ApiKey> byName = apiKeyMapper.findByUserIdAndExchangeName(user.getId(), "Kiwoom");
        assertThat(byName).isPresent();
    }

    @Test
    @Order(5)
    @DisplayName("API 키 상태 및 통계 테스트")
    void testStatusAndStats() {
        apiKeyMapper.insert(apiKey);

        ApiKey stockKey = ApiKey.builder()
                .userId(user.getId())
                .exchangeType(ExchangeType.KIWOOM)
                .exchangeName("Kiwoom")
                .accessKey("a2")
                .secretKey("s2")
                .apiPermissions("trade")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        apiKeyMapper.insert(stockKey);

        apiKeyMapper.updateLastUsed(apiKey.getId());
        apiKeyMapper.updateActiveStatus(stockKey.getId(), false);

        assertThat(apiKeyMapper.countActiveApiKeysByUserId(user.getId())).isEqualTo(1);
        assertThat(apiKeyMapper.findCryptoApiKeysByUserId(user.getId())).hasSize(1);
        assertThat(apiKeyMapper.findStockApiKeysByUserId(user.getId())).hasSize(1);
        assertThat(apiKeyMapper.findAllActiveExchanges()).contains("Upbit");
    }
}
