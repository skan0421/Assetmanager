package com.assetmanager.mapper;

import com.assetmanager.domain.User;
import com.assetmanager.domain.AuthProvider;
import com.assetmanager.domain.Role;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * User 도메인을 위한 MyBatis Mapper 인터페이스
 * Phase 2.3: MyBatis Mapper 구현 - UserMapper (완전 구현)
 */
@Mapper
public interface UserMapper {
    
    // =================
    // 기본 CRUD 연산
    // =================
    
    /**
     * 사용자 등록
     */
    @Insert("INSERT INTO users (email, password, name, auth_provider, role, is_active, created_at, updated_at) " +
            "VALUES (#{email}, #{password}, #{name}, #{authProvider}, #{role}, #{isActive}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(User user);
    
    /**
     * ID로 사용자 조회
     */
    @Select("SELECT * FROM users WHERE id = #{id}")
    Optional<User> findById(Long id);
    
    /**
     * 이메일로 사용자 조회
     */
    @Select("SELECT * FROM users WHERE email = #{email}")
    Optional<User> findByEmail(String email);
    
    /**
     * 사용자 정보 수정
     */
    @Update("UPDATE users SET name = #{name}, updated_at = NOW() WHERE id = #{id}")
    void update(User user);
    
    /**
     * 사용자 삭제 (소프트 삭제)
     */
    @Update("UPDATE users SET is_active = false, updated_at = NOW() WHERE id = #{id}")
    void softDelete(Long id);
    
    /**
     * 마지막 로그인 시간 업데이트
     */
    @Update("UPDATE users SET last_login_at = NOW(), updated_at = NOW() WHERE id = #{id}")
    void updateLastLogin(Long id);
    
    // =================
    // 조회 및 검색
    // =================
    
    /**
     * 모든 사용자 조회 (테스트용)
     */
    @Select("SELECT * FROM users ORDER BY created_at DESC")
    List<User> findAll();
    
    /**
     * 활성 사용자만 조회
     */
    @Select("SELECT * FROM users WHERE is_active = true ORDER BY created_at DESC")
    List<User> findActiveUsers();
    
    /**
     * 소셜 로그인 제공자별 사용자 조회
     */
    @Select("SELECT * FROM users WHERE auth_provider = #{authProvider} AND is_active = true ORDER BY created_at DESC")
    List<User> findByAuthProvider(@Param("authProvider") AuthProvider authProvider);
    
    /**
     * 역할별 사용자 조회
     */
    @Select("SELECT * FROM users WHERE role = #{role} AND is_active = true ORDER BY created_at DESC")
    List<User> findByRole(@Param("role") Role role);
    
    /**
     * 활성 사용자 수 조회
     */
    @Select("SELECT COUNT(*) FROM users WHERE is_active = true")
    int countActiveUsers();
    
    /**
     * 총 사용자 수 조회
     */
    @Select("SELECT COUNT(*) FROM users")
    int countTotalUsers();
    
    /**
     * 소셜 로그인 사용자 수 조회
     */
    @Select("SELECT COUNT(*) FROM users WHERE auth_provider != 'LOCAL' AND is_active = true")
    int countSocialLoginUsers();
    
    /**
     * 이메일 중복 확인
     */
    @Select("SELECT COUNT(*) FROM users WHERE email = #{email}")
    int countByEmail(String email);
    
    /**
     * 최근 가입 사용자 조회 (N명)
     */
    @Select("SELECT * FROM users WHERE is_active = true " +
            "ORDER BY created_at DESC LIMIT #{limit}")
    List<User> findRecentUsers(@Param("limit") int limit);
    
    /**
     * 최근 로그인 사용자 조회 (N명)
     */
    @Select("SELECT * FROM users WHERE last_login_at IS NOT NULL AND is_active = true " +
            "ORDER BY last_login_at DESC LIMIT #{limit}")
    List<User> findRecentLoginUsers(@Param("limit") int limit);
}
