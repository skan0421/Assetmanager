package com.assetmanager.mapper;

import com.assetmanager.domain.Transaction;
import com.assetmanager.domain.TransactionType;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Transaction 도메인을 위한 MyBatis Mapper 인터페이스
 * Phase 2.3: MyBatis Mapper 구현 - TransactionMapper
 */
@Mapper
public interface TransactionMapper {
    
    // =================
    // 기본 CRUD 연산
    // =================
    
    /**
     * 거래 내역 등록
     */
    @Insert("INSERT INTO transactions (user_id, asset_id, transaction_type, quantity, price, " +
            "total_amount, fee, tax, net_amount, transaction_date, notes, external_id, created_at) " +
            "VALUES (#{userId}, #{assetId}, #{transactionType}, #{quantity}, #{price}, " +
            "#{totalAmount}, #{fee}, #{tax}, #{netAmount}, #{transactionDate}, #{notes}, #{externalId}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Transaction transaction);
    
    /**
     * ID로 거래 내역 조회
     */
    @Select("SELECT * FROM transactions WHERE id = #{id}")
    Optional<Transaction> findById(Long id);
    
    /**
     * 거래 내역 수정 (가격, 수수료 등 수정 가능)
     */
    @Update("UPDATE transactions SET quantity = #{quantity}, price = #{price}, " +
            "total_amount = #{totalAmount}, fee = #{fee}, tax = #{tax}, net_amount = #{netAmount}, " +
            "transaction_date = #{transactionDate}, notes = #{notes}, external_id = #{externalId}, " +
            "updated_at = NOW() WHERE id = #{id}")
    void update(Transaction transaction);
    
    /**
     * 거래 내역 삭제
     */
    @Delete("DELETE FROM transactions WHERE id = #{id}")
    void delete(Long id);
    
    // =================
    // 사용자별 거래 내역 조회
    // =================
    
    /**
     * 사용자의 모든 거래 내역 조회 (최신순)
     */
    @Select("SELECT * FROM transactions WHERE user_id = #{userId} " +
            "ORDER BY transaction_date DESC, created_at DESC")
    List<Transaction> findByUserId(Long userId);
    
    /**
     * 사용자의 거래 내역 페이징 조회
     */
    @Select("SELECT * FROM transactions WHERE user_id = #{userId} " +
            "ORDER BY transaction_date DESC, created_at DESC " +
            "LIMIT #{limit} OFFSET #{offset}")
    List<Transaction> findByUserIdWithPaging(@Param("userId") Long userId,
                                           @Param("limit") int limit,
                                           @Param("offset") int offset);
    
    /**
     * 특정 자산의 거래 내역 조회
     */
    @Select("SELECT * FROM transactions WHERE user_id = #{userId} AND asset_id = #{assetId} " +
            "ORDER BY transaction_date DESC")
    List<Transaction> findByUserIdAndAssetId(@Param("userId") Long userId,
                                          @Param("assetId") Long assetId);
    
    /**
     * 거래 타입별 조회 (매수/매도)
     */
    @Select("SELECT * FROM transactions WHERE user_id = #{userId} " +
            "AND transaction_type = #{transactionType} " +
            "ORDER BY transaction_date DESC")
    List<Transaction> findByUserIdAndTransactionType(@Param("userId") Long userId,
                                                   @Param("transactionType") TransactionType transactionType);
    
    // =================
    // 기간별 거래 내역 조회
    // =================
    
    /**
     * 특정 기간의 거래 내역 조회
     */
    @Select("SELECT * FROM transactions WHERE user_id = #{userId} " +
            "AND transaction_date BETWEEN #{startDate} AND #{endDate} " +
            "ORDER BY transaction_date DESC")
    List<Transaction> findByUserIdAndDateRange(@Param("userId") Long userId,
                                             @Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);
    
    /**
     * 최근 N일간의 거래 내역 조회
     */
    @Select("SELECT * FROM transactions WHERE user_id = #{userId} " +
            "AND transaction_date >= #{fromDate} " +
            "ORDER BY transaction_date DESC")
    List<Transaction> findRecentTransactions(@Param("userId") Long userId,
                                           @Param("fromDate") LocalDateTime fromDate);
    
    // =================
    // 통계 및 집계 쿼리
    // =================
    
    /**
     * 사용자의 총 거래 횟수
     */
    @Select("SELECT COUNT(*) FROM transactions WHERE user_id = #{userId}")
    int countTransactionsByUserId(Long userId);
    
    /**
     * 총 매수 금액 계산
     */
    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM transactions " +
            "WHERE user_id = #{userId} AND transaction_type = 'BUY'")
    BigDecimal getTotalBuyAmountByUserId(Long userId);
    
    /**
     * 총 매도 금액 계산
     */
    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM transactions " +
            "WHERE user_id = #{userId} AND transaction_type = 'SELL'")
    BigDecimal getTotalSellAmountByUserId(Long userId);
    
    /**
     * 총 거래 수수료 계산
     */
    @Select("SELECT COALESCE(SUM(fee), 0) FROM transactions WHERE user_id = #{userId}")
    BigDecimal getTotalFeeByUserId(Long userId);
    
    /**
     * 특정 자산의 평균 매수가 계산
     */
    @Select("SELECT " +
            "CASE WHEN SUM(CASE WHEN transaction_type = 'BUY' THEN quantity ELSE 0 END) > 0 " +
            "THEN SUM(CASE WHEN transaction_type = 'BUY' THEN total_amount ELSE 0 END) / " +
            "     SUM(CASE WHEN transaction_type = 'BUY' THEN quantity ELSE 0 END) " +
            "ELSE 0 END as average_price " +
            "FROM transactions " +
            "WHERE user_id = #{userId} AND asset_id = #{assetId}")
    BigDecimal getAveragePurchasePrice(@Param("userId") Long userId,
                                     @Param("assetId") Long assetId);
    
    /**
     * 월별 거래 통계
     */
    @Select("SELECT " +
            "DATE_FORMAT(transaction_date, '%Y-%m') as month, " +
            "COUNT(*) as transaction_count, " +
            "SUM(total_amount) as total_amount " +
            "FROM transactions " +
            "WHERE user_id = #{userId} " +
            "GROUP BY DATE_FORMAT(transaction_date, '%Y-%m') " +
            "ORDER BY month DESC")
    List<Object> getMonthlyTransactionStats(Long userId);
    
    // =================
    // 최근 거래 조회
    // =================
    
    /**
     * 최근 거래 내역 N개 조회
     */
    @Select("SELECT * FROM transactions WHERE user_id = #{userId} " +
            "ORDER BY transaction_date DESC, created_at DESC LIMIT #{limit}")
    List<Transaction> findRecentTransactionsByUserId(@Param("userId") Long userId,
                                                   @Param("limit") int limit);
    
    /**
     * 특정 자산의 최근 거래 조회
     */
    @Select("SELECT * FROM transactions WHERE asset_id = #{assetId} " +
            "ORDER BY transaction_date DESC LIMIT #{limit}")
    List<Transaction> findRecentTransactionsByAssetId(@Param("assetId") Long assetId,
                                                    @Param("limit") int limit);
}
