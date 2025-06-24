package com.assetmanager.mapper;

import com.assetmanager.domain.ApiKey;
import com.assetmanager.domain.ExchangeType;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ApiKey 도메인을 위한 MyBatis Mapper 인터페이스
 */
@Mapper
public interface ApiKeyMapper {

    // =================
    // 기본 CRUD
    // =================

    /**
     * API 키 등록
     */
    @Insert("INSERT INTO api_keys (user_id, exchange_type, exchange_name, access_key, secret_key, api_permissions, is_active, last_used_at, expires_at, created_at, updated_at) " +
            "VALUES (#{userId}, #{exchangeType}, #{exchangeName}, #{accessKey}, #{secretKey}, #{apiPermissions}, #{isActive}, #{lastUsedAt}, #{expiresAt}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ApiKey apiKey);

    /**
     * ID로 API 키 조회
     */
    @Select("SELECT id, user_id, exchange_type, exchange_name, access_key, secret_key, api_permissions, is_active, last_used_at AS lastUsedAt, expires_at AS expiresAt " +
            "FROM api_keys WHERE id = #{id}")
    Optional<ApiKey> findById(Long id);

    /**
     * API 키 정보 수정
     */
    @Update("UPDATE api_keys SET exchange_name = #{exchangeName}, access_key = #{accessKey}, secret_key = #{secretKey}, " +
            "api_permissions = #{apiPermissions}, is_active = #{isActive}, last_used_at = #{lastUsedAt}, expires_at = #{expiresAt}, updated_at = NOW() WHERE id = #{id}")
    void update(ApiKey apiKey);

    /**
     * API 키 삭제
     */
    @Delete("DELETE FROM api_keys WHERE id = #{id}")
    void delete(Long id);

    /**
     * API 키 비활성화
     */
    @Update("UPDATE api_keys SET is_active = false, updated_at = NOW() WHERE id = #{id}")
    void deactivate(Long id);

    // =================
    // 사용자별 조회
    // =================

    @Select("SELECT id, user_id, exchange_type, exchange_name, access_key, secret_key, api_permissions, is_active, last_used_at AS lastUsedAt, expires_at AS expiresAt " +
            "FROM api_keys WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<ApiKey> findByUserId(Long userId);

    @Select("SELECT id, user_id, exchange_type, exchange_name, access_key, secret_key, api_permissions, is_active, last_used_at AS lastUsedAt, expires_at AS expiresAt " +
            "FROM api_keys WHERE user_id = #{userId} AND is_active = true ORDER BY created_at DESC")
    List<ApiKey> findActiveApiKeysByUserId(Long userId);

    @Select("SELECT id, user_id, exchange_type, exchange_name, access_key, secret_key, api_permissions, is_active, last_used_at AS lastUsedAt, expires_at AS expiresAt " +
            "FROM api_keys WHERE user_id = #{userId} AND exchange_type = #{exchangeType}")
    Optional<ApiKey> findByUserIdAndExchangeType(@Param("userId") Long userId,
                                                @Param("exchangeType") ExchangeType exchangeType);

    @Select("SELECT id, user_id, exchange_type, exchange_name, access_key, secret_key, api_permissions, is_active, last_used_at AS lastUsedAt, expires_at AS expiresAt " +
            "FROM api_keys WHERE user_id = #{userId} AND exchange_name = #{exchangeName}")
    Optional<ApiKey> findByUserIdAndExchangeName(@Param("userId") Long userId,
                                                @Param("exchangeName") String exchangeName);

    // =================
    // 보안 및 상태 관리
    // =================

    @Update("UPDATE api_keys SET last_used_at = NOW() WHERE id = #{id}")
    void updateLastUsed(Long id);

    @Update("UPDATE api_keys SET is_active = #{active}, updated_at = NOW() WHERE id = #{id}")
    void updateActiveStatus(@Param("id") Long id, @Param("active") boolean active);

    @Select("SELECT id, user_id, exchange_type, exchange_name, access_key, secret_key, api_permissions, is_active, last_used_at AS lastUsedAt, expires_at AS expiresAt " +
            "FROM api_keys WHERE expires_at < NOW() AND is_active = true")
    List<ApiKey> findExpiredApiKeys();

    @Select("SELECT id, user_id, exchange_type, exchange_name, access_key, secret_key, api_permissions, is_active, last_used_at AS lastUsedAt, expires_at AS expiresAt " +
            "FROM api_keys WHERE expires_at <= #{before}")
    List<ApiKey> findApiKeysExpiringBefore(@Param("before") LocalDateTime before);

    @Select("SELECT COUNT(*) FROM api_keys WHERE user_id = #{userId} AND is_active = true")
    int countActiveApiKeysByUserId(Long userId);

    // =================
    // 거래소 타입별 조회
    // =================

    @Select("SELECT id, user_id, exchange_type, exchange_name, access_key, secret_key, api_permissions, is_active, last_used_at AS lastUsedAt, expires_at AS expiresAt " +
            "FROM api_keys WHERE user_id = #{userId} AND exchange_type = 'CRYPTO'")
    List<ApiKey> findCryptoApiKeysByUserId(Long userId);

    @Select("SELECT id, user_id, exchange_type, exchange_name, access_key, secret_key, api_permissions, is_active, last_used_at AS lastUsedAt, expires_at AS expiresAt " +
            "FROM api_keys WHERE user_id = #{userId} AND exchange_type = 'STOCK'")
    List<ApiKey> findStockApiKeysByUserId(Long userId);

    @Select("SELECT DISTINCT exchange_name FROM api_keys WHERE is_active = true")
    List<String> findAllActiveExchanges();

    // =================
    // 권한 관리 (문자열 기반)
    // =================

    /**
     * 거래 권한이 있는 API 키 조회
     */
    @Select("SELECT id, user_id, exchange_type, exchange_name, access_key, secret_key, api_permissions, is_active, last_used_at AS lastUsedAt, expires_at AS expiresAt " +
            "FROM api_keys WHERE user_id = #{userId} AND (api_permissions LIKE '%trade%' OR api_permissions = 'trade')")
    List<ApiKey> findTradingApiKeysByUserId(Long userId);

    /**
     * 읽기 전용 API 키 조회
     */
    @Select("SELECT id, user_id, exchange_type, exchange_name, access_key, secret_key, api_permissions, is_active, last_used_at AS lastUsedAt, expires_at AS expiresAt " +
            "FROM api_keys WHERE user_id = #{userId} AND (api_permissions NOT LIKE '%trade%' AND api_permissions LIKE '%read%')")
    List<ApiKey> findReadOnlyApiKeysByUserId(Long userId);

    // =================
    // 배치 작업
    // =================

    @Update("UPDATE api_keys SET is_active = false WHERE expires_at < NOW() AND is_active = true")
    void deactivateExpiredApiKeys();

    @Delete("DELETE FROM api_keys WHERE is_active = false AND updated_at < #{before}")
    void deleteOldInactiveApiKeys(@Param("before") LocalDateTime before);
}
