package com.assetmanager.mapper;

import com.assetmanager.domain.Asset;
import com.assetmanager.domain.AssetType;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Asset 도메인을 위한 MyBatis Mapper 인터페이스
 * Phase 2.3: MyBatis Mapper 구현 - AssetMapper
 */
@Mapper
public interface AssetMapper {
    
    // =================
    // 기본 CRUD 연산
    // =================
    
    /**
     * 자산 등록
     */
    @Insert("INSERT INTO assets (user_id, symbol, name, asset_type, exchange, quantity, average_price, created_at, updated_at) " +
            "VALUES (#{userId}, #{symbol}, #{name}, #{assetType}, #{exchange}, #{quantity}, #{averagePrice}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Asset asset);
    
    /**
     * ID로 자산 조회
     */
    @Select("SELECT * FROM assets WHERE id = #{id}")
    Optional<Asset> findById(Long id);
    
    /**
     * 자산 정보 수정
     */
    @Update("UPDATE assets SET quantity = #{quantity}, average_price = #{averagePrice}, " +
            "updated_at = NOW() WHERE id = #{id}")
    void update(Asset asset);
    
    /**
     * 자산 삭제 (실제로는 비활성화)
     */
    @Update("UPDATE assets SET is_active = false, updated_at = NOW() WHERE id = #{id}")
    void softDelete(Long id);
    
    // =================
    // 사용자별 자산 조회
    // =================
    
    /**
     * 사용자의 모든 자산 조회 (비활성 포함)
     */
    @Select("SELECT * FROM assets WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<Asset> findByUserId(Long userId);
    
    /**
     * 사용자의 활성 자산만 조회
     */
    @Select("SELECT * FROM assets WHERE user_id = #{userId} AND is_active = true " +
            "ORDER BY asset_type, symbol")
    List<Asset> findActiveAssetsByUserId(Long userId);
    
    /**
     * 사용자의 특정 자산 타입별 조회
     */
    @Select("SELECT * FROM assets WHERE user_id = #{userId} AND asset_type = #{assetType} " +
            "AND is_active = true ORDER BY symbol")
    List<Asset> findByUserIdAndAssetType(@Param("userId") Long userId, 
                                        @Param("assetType") AssetType assetType);
    
    /**
     * 심볼로 자산 조회 (특정 사용자)
     */
    @Select("SELECT * FROM assets WHERE user_id = #{userId} AND symbol = #{symbol} " +
            "AND is_active = true")
    Optional<Asset> findByUserIdAndSymbol(@Param("userId") Long userId, 
                                         @Param("symbol") String symbol);
    
    // =================
    // 포트폴리오 계산용 쿼리
    // =================
    
    /**
     * 사용자의 총 투자 금액 계산
     */
    @Select("SELECT COALESCE(SUM(quantity * average_price), 0) " +
            "FROM assets WHERE user_id = #{userId} AND is_active = true")
    BigDecimal getTotalInvestmentByUserId(Long userId);
    
    /**
     * 사용자의 활성 자산 개수 조회
     */
    @Select("SELECT COUNT(*) FROM assets WHERE user_id = #{userId} AND is_active = true")
    int countActiveAssetsByUserId(Long userId);
    
    /**
     * 자산 타입별 투자 금액 계산
     */
    @Select("SELECT COALESCE(SUM(quantity * average_price), 0) " +
            "FROM assets WHERE user_id = #{userId} AND asset_type = #{assetType} " +
            "AND is_active = true")
    BigDecimal getInvestmentByAssetType(@Param("userId") Long userId, 
                                      @Param("assetType") AssetType assetType);
    
    /**
     * 가장 많이 투자한 자산 Top N
     */
    @Select("SELECT * FROM assets WHERE user_id = #{userId} AND is_active = true " +
            "ORDER BY (quantity * average_price) DESC LIMIT #{limit}")
    List<Asset> findTopInvestmentAssets(@Param("userId") Long userId, 
                                       @Param("limit") int limit);
    
    // =================
    // 비즈니스 로직용 쿼리
    // =================
    
    /**
     * 보유 수량이 있는 자산만 조회
     */
    @Select("SELECT * FROM assets WHERE user_id = #{userId} AND quantity > 0 " +
            "AND is_active = true ORDER BY symbol")
    List<Asset> findHoldingAssetsByUserId(Long userId);
    
    /**
     * 특정 거래소의 자산들 조회
     */
    @Select("SELECT * FROM assets WHERE user_id = #{userId} AND exchange = #{exchange} " +
            "AND is_active = true ORDER BY symbol")
    List<Asset> findByUserIdAndExchange(@Param("userId") Long userId, 
                                       @Param("exchange") String exchange);
}
