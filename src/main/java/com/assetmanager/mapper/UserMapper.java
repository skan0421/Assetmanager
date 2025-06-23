package com.assetmanager.mapper;

import com.assetmanager.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

/**
 * User 도메인을 위한 MyBatis Mapper 인터페이스
 * Phase 2.3: MyBatis Mapper 구현 - UserMapper
 */
@Mapper
public interface UserMapper {
    
    /**
     * 모든 사용자 조회 (테스트용)
     */
    @Select("SELECT * FROM users")
    List<User> findAll();
    
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
     * 활성 사용자 수 조회
     */
    @Select("SELECT COUNT(*) FROM users WHERE is_active = true")
    int countActiveUsers();
}
