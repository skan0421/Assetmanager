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
public class Transaction {
    private Long id;
    private Long userId;
    private Long assetId;
    private TransactionType transactionType;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal totalAmount;
    private BigDecimal fee;
    private BigDecimal tax;
    private BigDecimal netAmount;
    private LocalDateTime transactionDate;
    private String notes;
    private String externalId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BigDecimal calculateNetAmount() {
        BigDecimal net = totalAmount;
        if (fee != null) {
            net = net.subtract(fee);
        }
        if (tax != null) {
            net = net.subtract(tax);
        }
        return net;
    }

    public boolean isBuyTransaction() {
        return transactionType == TransactionType.BUY;
    }

    public boolean isSellTransaction() {
        return transactionType == TransactionType.SELL;
    }

    public BigDecimal getFeeRate() {
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) == 0 || fee == null) {
            return BigDecimal.ZERO;
        }
        return fee.divide(totalAmount, 8, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    public void validate() {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("price must not be negative");
        }
    }

    public String getSummary() {
        return String.format("%s %s %.4f @ %.2f", transactionType, symbolOrId(),
                quantity, price);
    }

    private String symbolOrId() {
        return assetId != null ? assetId.toString() : "-";
    }
}
