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
public class Asset {
    private Long id;
    private Long userId;
    private String symbol;
    private String name;
    private AssetType assetType;
    private String exchange;
    private String countryCode;
    private BigDecimal quantity;
    private BigDecimal averagePrice;
    private String currency;
    private Boolean isActive;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BigDecimal getTotalInvestmentAmount() {
        if (quantity == null || averagePrice == null) {
            return BigDecimal.ZERO;
        }
        return quantity.multiply(averagePrice);
    }

    public BigDecimal getCurrentValue(BigDecimal currentPrice) {
        if (currentPrice == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return currentPrice.multiply(quantity);
    }

    public BigDecimal getProfitLoss(BigDecimal currentPrice) {
        return getCurrentValue(currentPrice).subtract(getTotalInvestmentAmount());
    }

    public BigDecimal getProfitRate(BigDecimal currentPrice) {
        BigDecimal investment = getTotalInvestmentAmount();
        if (investment.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return getProfitLoss(currentPrice)
            .divide(investment, 8, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
    }

    public void addPurchase(BigDecimal buyQuantity, BigDecimal buyPrice) {
        if (buyQuantity == null || buyPrice == null ||
            buyQuantity.compareTo(BigDecimal.ZERO) <= 0 ||
            buyPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("invalid purchase data");
        }
        BigDecimal totalCost = getTotalInvestmentAmount()
            .add(buyQuantity.multiply(buyPrice));
        BigDecimal newQuantity = quantity.add(buyQuantity);
        this.averagePrice = totalCost.divide(newQuantity, 8, RoundingMode.HALF_UP);
        this.quantity = newQuantity;
    }

    public BigDecimal sell(BigDecimal sellQuantity) {
        if (sellQuantity == null || sellQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("sell quantity must be positive");
        }
        if (sellQuantity.compareTo(quantity) > 0) {
            throw new IllegalArgumentException("insufficient quantity");
        }
        this.quantity = this.quantity.subtract(sellQuantity);
        return sellQuantity.multiply(averagePrice);
    }

    public boolean isHolding() {
        return quantity != null && quantity.compareTo(BigDecimal.ZERO) > 0;
    }

    public BigDecimal getPortfolioWeight(BigDecimal totalPortfolioValue) {
        if (totalPortfolioValue == null || totalPortfolioValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return getCurrentValue(averagePrice)
            .divide(totalPortfolioValue, 8, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
    }
}
