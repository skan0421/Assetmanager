package com.assetmanager.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceHistory {
    private Long id;
    private String symbol;
    private String exchange;
    private BigDecimal price;
    private BigDecimal volume;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal openPrice;
    private BigDecimal closePrice;
    private BigDecimal changeRate;
    private LocalDateTime timestamp;

    public boolean hasPriceChanged() {
        return closePrice != null && openPrice != null && closePrice.compareTo(openPrice) != 0;
    }

    public boolean isPriceIncreased() {
        return closePrice != null && openPrice != null && closePrice.compareTo(openPrice) > 0;
    }

    public boolean isPriceDecreased() {
        return closePrice != null && openPrice != null && closePrice.compareTo(openPrice) < 0;
    }

    public BigDecimal getPriceRange() {
        if (highPrice == null || lowPrice == null) {
            return BigDecimal.ZERO;
        }
        return highPrice.subtract(lowPrice);
    }

    public BigDecimal getIntraDayChangeRate() {
        if (openPrice == null || openPrice.compareTo(BigDecimal.ZERO) == 0 || closePrice == null) {
            return BigDecimal.ZERO;
        }
        return closePrice.subtract(openPrice)
            .divide(openPrice, 8, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
    }

    public BigDecimal getVolatilityIndicator() {
        if (openPrice == null || openPrice.compareTo(BigDecimal.ZERO) == 0 || highPrice == null || lowPrice == null) {
            return BigDecimal.ZERO;
        }
        return highPrice.subtract(lowPrice)
            .divide(openPrice, 8, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
    }

    public boolean hasValidOHLCData() {
        return highPrice != null && lowPrice != null && openPrice != null && closePrice != null && highPrice.compareTo(lowPrice) >= 0;
    }

    public boolean isRecentData() {
        return timestamp != null && timestamp.isAfter(LocalDateTime.now().minusDays(1));
    }
}
