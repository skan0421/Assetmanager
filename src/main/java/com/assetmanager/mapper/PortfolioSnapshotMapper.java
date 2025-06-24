package com.assetmanager.mapper;

import com.assetmanager.domain.PortfolioSnapshot;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * PortfolioSnapshot 도메인을 위한 MyBatis Mapper 인터페이스
 */
@Mapper
public interface PortfolioSnapshotMapper {

    // =================
    // 기본 CRUD
    // =================

    /**
     * 스냅샷 등록
     */
    @Insert("INSERT INTO portfolio_snapshots (user_id, snapshot_date, total_investment, total_current_value, total_profit_loss, profit_rate, asset_count, crypto_value, stock_value, created_at) " +
            "VALUES (#{userId}, #{snapshotDate}, #{totalInvestment}, #{totalCurrentValue}, #{totalProfitLoss}, #{profitRate}, #{assetCount}, #{cryptoValue}, #{stockValue}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(PortfolioSnapshot snapshot);

    /**
     * ID로 스냅샷 조회
     */
    @Select("SELECT id, user_id, snapshot_date, total_investment, total_current_value, total_profit_loss, profit_rate, asset_count, crypto_value, stock_value " +
            "FROM portfolio_snapshots WHERE id = #{id}")
    Optional<PortfolioSnapshot> findById(Long id);

    /**
     * 스냅샷 수정
     */
    @Update("UPDATE portfolio_snapshots SET total_investment = #{totalInvestment}, total_current_value = #{totalCurrentValue}, total_profit_loss = #{totalProfitLoss}, " +
            "profit_rate = #{profitRate}, asset_count = #{assetCount}, crypto_value = #{cryptoValue}, stock_value = #{stockValue} WHERE id = #{id}")
    void update(PortfolioSnapshot snapshot);

    /**
     * 스냅샷 삭제
     */
    @Delete("DELETE FROM portfolio_snapshots WHERE id = #{id}")
    void delete(Long id);

    // =================
    // 사용자별 조회
    // =================

    @Select("SELECT id, user_id, snapshot_date, total_investment, total_current_value, total_profit_loss, profit_rate, asset_count, crypto_value, stock_value " +
            "FROM portfolio_snapshots WHERE user_id = #{userId} ORDER BY snapshot_date DESC")
    List<PortfolioSnapshot> findByUserId(Long userId);

    @Select("SELECT id, user_id, snapshot_date, total_investment, total_current_value, total_profit_loss, profit_rate, asset_count, crypto_value, stock_value " +
            "FROM portfolio_snapshots WHERE user_id = #{userId} ORDER BY snapshot_date DESC LIMIT #{limit} OFFSET #{offset}")
    List<PortfolioSnapshot> findByUserIdWithPaging(@Param("userId") Long userId,
                                                   @Param("limit") int limit,
                                                   @Param("offset") int offset);

    @Select("SELECT id, user_id, snapshot_date, total_investment, total_current_value, total_profit_loss, profit_rate, asset_count, crypto_value, stock_value " +
            "FROM portfolio_snapshots WHERE user_id = #{userId} AND snapshot_date = #{date}")
    Optional<PortfolioSnapshot> findByUserIdAndDate(@Param("userId") Long userId,
                                                   @Param("date") LocalDate date);

    @Select("SELECT id, user_id, snapshot_date, total_investment, total_current_value, total_profit_loss, profit_rate, asset_count, crypto_value, stock_value " +
            "FROM portfolio_snapshots WHERE user_id = #{userId} ORDER BY snapshot_date DESC LIMIT 1")
    Optional<PortfolioSnapshot> findLatestByUserId(Long userId);

    @Select("SELECT id, user_id, snapshot_date, total_investment, total_current_value, total_profit_loss, profit_rate, asset_count, crypto_value, stock_value " +
            "FROM portfolio_snapshots WHERE user_id = #{userId} AND snapshot_date BETWEEN #{start} AND #{end} ORDER BY snapshot_date")
    List<PortfolioSnapshot> findByUserIdAndDateRange(@Param("userId") Long userId,
                                                    @Param("start") LocalDate start,
                                                    @Param("end") LocalDate end);

    @Select("SELECT id, user_id, snapshot_date, total_investment, total_current_value, total_profit_loss, profit_rate, asset_count, crypto_value, stock_value " +
            "FROM portfolio_snapshots WHERE user_id = #{userId} AND snapshot_date >= #{fromDate} ORDER BY snapshot_date DESC")
    List<PortfolioSnapshot> findRecentSnapshots(@Param("userId") Long userId,
                                               @Param("fromDate") LocalDate fromDate);

    // =================
    // 통계 및 집계
    // =================

    @Select("SELECT MAX(profit_rate) FROM portfolio_snapshots WHERE user_id = #{userId} AND snapshot_date BETWEEN #{start} AND #{end}")
    BigDecimal getMaxProfitRateInPeriod(@Param("userId") Long userId,
                                        @Param("start") LocalDate start,
                                        @Param("end") LocalDate end);

    @Select("SELECT MIN(profit_rate) FROM portfolio_snapshots WHERE user_id = #{userId} AND snapshot_date BETWEEN #{start} AND #{end}")
    BigDecimal getMinProfitRateInPeriod(@Param("userId") Long userId,
                                        @Param("start") LocalDate start,
                                        @Param("end") LocalDate end);

    @Select("SELECT AVG(profit_rate) FROM portfolio_snapshots WHERE user_id = #{userId} AND snapshot_date BETWEEN #{start} AND #{end}")
    BigDecimal getAverageProfitRateInPeriod(@Param("userId") Long userId,
                                            @Param("start") LocalDate start,
                                            @Param("end") LocalDate end);

    @Select("SELECT MAX(total_current_value) FROM portfolio_snapshots WHERE user_id = #{userId}")
    BigDecimal getMaxPortfolioValue(Long userId);

    @Select("SELECT COUNT(*) FROM portfolio_snapshots WHERE user_id = #{userId} AND total_profit_loss > 0")
    int countProfitDays(Long userId);

    @Select("SELECT COUNT(*) FROM portfolio_snapshots WHERE user_id = #{userId} AND total_profit_loss < 0")
    int countLossDays(Long userId);

    @Select("SELECT (SUM(total_current_value) - SUM(total_investment)) / SUM(total_investment) * 100 FROM portfolio_snapshots WHERE user_id = #{userId}")
    BigDecimal getTotalGrowthRate(Long userId);

    @Select("SELECT STDDEV(profit_rate) FROM portfolio_snapshots WHERE user_id = #{userId}")
    BigDecimal getProfitRateVolatility(Long userId);

    @Select("SELECT AVG(crypto_value / NULLIF(total_current_value,0) * 100) FROM portfolio_snapshots WHERE user_id = #{userId}")
    BigDecimal getAverageCryptoWeight(Long userId);

    @Select("SELECT AVG(stock_value / NULLIF(total_current_value,0) * 100) FROM portfolio_snapshots WHERE user_id = #{userId}")
    BigDecimal getAverageStockWeight(Long userId);

    // =================
    // 데이터 관리
    // =================

    @Insert({
        "<script>",
        "INSERT INTO portfolio_snapshots (user_id, snapshot_date, total_investment, total_current_value, total_profit_loss, profit_rate, asset_count, crypto_value, stock_value, created_at)",
        "VALUES",
        "<foreach collection='list' item='item' separator=','>",
        "(#{item.userId}, #{item.snapshotDate}, #{item.totalInvestment}, #{item.totalCurrentValue}, #{item.totalProfitLoss}, #{item.profitRate}, #{item.assetCount}, #{item.cryptoValue}, #{item.stockValue}, NOW())",
        "</foreach>",
        "</script>"
    })
    void insertBatch(@Param("list") List<PortfolioSnapshot> list);

    @Insert("INSERT INTO portfolio_snapshots (user_id, snapshot_date, total_investment, total_current_value, total_profit_loss, profit_rate, asset_count, crypto_value, stock_value, created_at) " +
            "VALUES (#{userId}, #{snapshotDate}, #{totalInvestment}, #{totalCurrentValue}, #{totalProfitLoss}, #{profitRate}, #{assetCount}, #{cryptoValue}, #{stockValue}, NOW()) " +
            "ON DUPLICATE KEY UPDATE total_investment = #{totalInvestment}, total_current_value = #{totalCurrentValue}, total_profit_loss = #{totalProfitLoss}, profit_rate = #{profitRate}, asset_count = #{assetCount}, crypto_value = #{cryptoValue}, stock_value = #{stockValue}")
    void upsertSnapshot(PortfolioSnapshot snapshot);

    @Delete("DELETE FROM portfolio_snapshots WHERE snapshot_date < #{before}")
    void deleteOldSnapshots(@Param("before") LocalDate before);
}
