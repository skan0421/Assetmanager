package com.assetmanager.domain;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

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
public class ApiKey {
    private Long id;
    private Long userId;
    private ExchangeType exchangeType;
    private String exchangeName;
    private String accessKey;
    private String secretKey;
    private Set<String> apiPermissions;
    private Boolean isActive;
    private LocalDateTime lastUsedAt;
    private LocalDateTime expiresAt;

    public boolean isActiveAndValid() {
        return Boolean.TRUE.equals(isActive) && !isExpired();
    }

    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    public void updateLastUsed() {
        this.lastUsedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.isActive = false;
    }

    public boolean hasPermission(String permission) {
        if (apiPermissions == null) {
            return false;
        }
        return apiPermissions.contains(permission);
    }

    public boolean canTrade() {
        return hasPermission("trade");
    }

    public boolean canRead() {
        return hasPermission("read");
    }

    public boolean isCryptoExchange() {
        return exchangeType == ExchangeType.CRYPTO;
    }

    public boolean isStockBroker() {
        return exchangeType == ExchangeType.STOCK;
    }

    public Set<String> getApiPermissions() {
        return apiPermissions == null ? Collections.emptySet() : apiPermissions;
    }
}
