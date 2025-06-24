package com.assetmanager.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
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
public class PortfolioSnapshot {
    private Long id;
    private Long userId;
    private LocalDate snapshotDate;
    private BigDecimal totalInvestment;
    private BigDecimal totalCurrentValue;
    private BigDecimal totalProfitLoss;
    private BigDecimal profitRate;
    private Integer assetCount;
    private BigDecimal cryptoValue;
    private BigDecimal stockValue;
    private String notes;
    private LocalDateTime createdAt;

    public void recalculateProfitRate() {
        if (totalInvestment == null || totalInvestment.compareTo(BigDecimal.ZERO) == 0) {
            this.profitRate = BigDecimal.ZERO;
        } else {
            this.profitRate = totalProfitLoss
                .divide(totalInvestment, 8, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        }
    }

    public void recalculateProfitLoss() {
        if (totalCurrentValue == null || totalInvestment == null) {
            this.totalProfitLoss = BigDecimal.ZERO;
        } else {
            this.totalProfitLoss = totalCurrentValue.subtract(totalInvestment);
        }
    }

    public boolean isProfit() {
        return totalProfitLoss != null && totalProfitLoss.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isLoss() {
        return totalProfitLoss != null && totalProfitLoss.compareTo(BigDecimal.ZERO) < 0;
    }

    public BigDecimal getCryptoWeight() {
        if (totalCurrentValue == null || totalCurrentValue.compareTo(BigDecimal.ZERO) == 0 || cryptoValue == null) {
            return BigDecimal.ZERO;
        }
        return cryptoValue.divide(totalCurrentValue, 8, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
    }

    public BigDecimal getStockWeight() {
        if (totalCurrentValue == null || totalCurrentValue.compareTo(BigDecimal.ZERO) == 0 || stockValue == null) {
            return BigDecimal.ZERO;
        }
        return stockValue.divide(totalCurrentValue, 8, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
    }

    public BigDecimal getDiversificationIndex() {
        BigDecimal cryptoWeight = getCryptoWeight();
        BigDecimal stockWeight = getStockWeight();
        return cryptoWeight.subtract(stockWeight).abs();
    }

    public String getSummary() {
        return String.format("%s: %s%%", snapshotDate, profitRate);
    }

    public boolean isTodaySnapshot() {
        return snapshotDate != null && snapshotDate.equals(LocalDate.now());
    }
}
