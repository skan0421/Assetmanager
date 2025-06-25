package com.assetmanager.mapper;

import com.assetmanager.domain.PriceHistory;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * PriceHistory 도메인을 위한 MyBatis Mapper 인터페이스
 */
@Mapper
public interface PriceHistoryMapper {

    // =================
    // 기본 CRUD
    // =================

    /**
     * 가격 히스토리 등록
     */
    @Insert("INSERT INTO price_history (symbol, exchange, price, volume, market_cap, high_price, low_price, open_price, close_price, change_rate, data_source, price_timestamp, created_at) " +
            "VALUES (#{symbol}, #{exchange}, #{price}, #{volume}, #{marketCap}, #{highPrice}, #{lowPrice}, #{openPrice}, #{closePrice}, #{changeRate}, #{dataSource}, #{priceTimestamp}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(PriceHistory priceHistory);

    /**
     * ID로 가격 히스토리 조회
     */
    @Select("SELECT id, symbol, exchange, price, volume, market_cap, high_price, low_price, open_price, close_price, change_rate, data_source, price_timestamp AS priceTimestamp " +
            "FROM price_history WHERE id = #{id}")
    Optional<PriceHistory> findById(Long id);

    /**
     * 가격 정보 수정
     */
    @Update("UPDATE price_history SET price = #{price}, volume = #{volume}, market_cap = #{marketCap}, high_price = #{highPrice}, low_price = #{lowPrice}, " +
            "open_price = #{openPrice}, close_price = #{closePrice}, change_rate = #{changeRate}, data_source = #{dataSource}, price_timestamp = #{priceTimestamp} WHERE id = #{id}")
    void update(PriceHistory priceHistory);

    /**
     * 가격 데이터 삭제
     */
    @Delete("DELETE FROM price_history WHERE id = #{id}")
    void delete(Long id);

    // =================
    // 심볼 및 기간별 조회
    // =================

    /**
     * 특정 심볼의 최신 가격 조회
     */
    @Select("SELECT id, symbol, exchange, price, volume, market_cap, high_price, low_price, open_price, close_price, change_rate, data_source, price_timestamp AS priceTimestamp " +
            "FROM price_history WHERE symbol = #{symbol} ORDER BY price_timestamp DESC LIMIT 1")
    Optional<PriceHistory> findLatestBySymbol(String symbol);

    /**
     * 최근 N개 가격 데이터 조회
     */
    @Select("SELECT id, symbol, exchange, price, volume, market_cap, high_price, low_price, open_price, close_price, change_rate, data_source, price_timestamp AS priceTimestamp " +
            "FROM price_history WHERE symbol = #{symbol} ORDER BY price_timestamp DESC LIMIT #{limit}")
    List<PriceHistory> findBySymbolWithLimit(@Param("symbol") String symbol,
                                            @Param("limit") int limit);

    /**
     * 거래소별 최신 가격 조회
     */
    @Select("SELECT id, symbol, exchange, price, volume, market_cap, high_price, low_price, open_price, close_price, change_rate, data_source, price_timestamp AS priceTimestamp " +
            "FROM price_history WHERE symbol = #{symbol} AND exchange = #{exchange} ORDER BY price_timestamp DESC LIMIT 1")
    Optional<PriceHistory> findLatestBySymbolAndExchange(@Param("symbol") String symbol,
                                                        @Param("exchange") String exchange);

    /**
     * 기간별 가격 데이터 조회
     */
    @Select("SELECT id, symbol, exchange, price, volume, market_cap, high_price, low_price, open_price, close_price, change_rate, data_source, price_timestamp AS priceTimestamp " +
            "FROM price_history WHERE symbol = #{symbol} AND price_timestamp BETWEEN #{start} AND #{end} ORDER BY price_timestamp")
    List<PriceHistory> findBySymbolAndDateRange(@Param("symbol") String symbol,
                                               @Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end);

    /**
     * 최근 기간 가격 데이터 조회
     */
    @Select("SELECT id, symbol, exchange, price, volume, market_cap, high_price, low_price, open_price, close_price, change_rate, data_source, price_timestamp AS priceTimestamp " +
            "FROM price_history WHERE symbol = #{symbol} AND price_timestamp >= #{fromDate} ORDER BY price_timestamp DESC")
    List<PriceHistory> findRecentPriceHistory(@Param("symbol") String symbol,
                                             @Param("fromDate") LocalDateTime fromDate);

    // =================
    // 가격 분석 쿼리
    // =================

    @Select("SELECT MAX(price) FROM price_history WHERE symbol = #{symbol} AND price_timestamp BETWEEN #{start} AND #{end}")
    BigDecimal getMaxPriceInPeriod(@Param("symbol") String symbol,
                                   @Param("start") LocalDateTime start,
                                   @Param("end") LocalDateTime end);

    @Select("SELECT MIN(price) FROM price_history WHERE symbol = #{symbol} AND price_timestamp BETWEEN #{start} AND #{end}")
    BigDecimal getMinPriceInPeriod(@Param("symbol") String symbol,
                                   @Param("start") LocalDateTime start,
                                   @Param("end") LocalDateTime end);

    @Select("SELECT AVG(price) FROM price_history WHERE symbol = #{symbol} AND price_timestamp BETWEEN #{start} AND #{end}")
    BigDecimal getAveragePriceInPeriod(@Param("symbol") String symbol,
                                       @Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end);

    @Select("SELECT COALESCE(SUM(volume),0) FROM price_history WHERE symbol = #{symbol} AND price_timestamp BETWEEN #{start} AND #{end}")
    BigDecimal getTotalVolumeInPeriod(@Param("symbol") String symbol,
                                      @Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end);

    // =================
    // 데이터 관리
    // =================

    @Insert({
        "<script>",
        "INSERT INTO price_history (symbol, exchange, price, volume, market_cap, high_price, low_price, open_price, close_price, change_rate, data_source, price_timestamp, created_at)",
        "VALUES",
        "<foreach collection='list' item='item' separator=','>",
        "(#{item.symbol}, #{item.exchange}, #{item.price}, #{item.volume}, #{item.marketCap}, #{item.highPrice}, #{item.lowPrice}, #{item.openPrice}, #{item.closePrice}, #{item.changeRate}, #{item.dataSource}, #{item.priceTimestamp}, NOW())",
        "</foreach>",
        "</script>"
    })
    void insertBatch(@Param("list") List<PriceHistory> list);

    @Insert("INSERT INTO price_history (symbol, exchange, price, volume, market_cap, high_price, low_price, open_price, close_price, change_rate, data_source, price_timestamp, created_at) " +
            "VALUES (#{symbol}, #{exchange}, #{price}, #{volume}, #{marketCap}, #{highPrice}, #{lowPrice}, #{openPrice}, #{closePrice}, #{changeRate}, #{dataSource}, #{priceTimestamp}, NOW()) " +
            "ON DUPLICATE KEY UPDATE price = #{price}, volume = #{volume}, market_cap = #{marketCap}, high_price = #{highPrice}, low_price = #{lowPrice}, open_price = #{openPrice}, close_price = #{closePrice}, change_rate = #{changeRate}, data_source = #{dataSource}, price_timestamp = #{priceTimestamp}")
    void upsertPriceHistory(PriceHistory priceHistory);

    @Delete("DELETE FROM price_history WHERE price_timestamp < #{before}")
    void deleteOldPriceData(@Param("before") LocalDateTime before);

    @Select("SELECT COUNT(*) FROM price_history WHERE symbol = #{symbol} AND exchange = #{exchange} AND price_timestamp = #{priceTimestamp}")
    int countDuplicateData(@Param("symbol") String symbol,
                           @Param("exchange") String exchange,
                           @Param("priceTimestamp") LocalDateTime priceTimestamp);
}
