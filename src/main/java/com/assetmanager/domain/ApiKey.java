package com.assetmanager.domain;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
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
    private String apiPermissions; // JSON 문자열로 저장 (ex: "read,trade")
    private Boolean isActive;
    private LocalDateTime lastUsedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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
        if (apiPermissions == null || apiPermissions.isEmpty()) {
            return false;
        }
        return getApiPermissionsSet().contains(permission);
    }

    public boolean canTrade() {
        return hasPermission("trade");
    }

    public boolean canRead() {
        return hasPermission("read");
    }

    public boolean isCryptoExchange() {
        return exchangeType == ExchangeType.UPBIT || exchangeType == ExchangeType.BITHUMB;
    }

    public boolean isStockBroker() {
        return exchangeType == ExchangeType.KIWOOM || exchangeType == ExchangeType.KIS;
    }

    /**
     * 문자열로 저장된 권한을 Set으로 변환
     */
    public Set<String> getApiPermissionsSet() {
        if (apiPermissions == null || apiPermissions.trim().isEmpty()) {
            return Collections.emptySet();
        }
        return new HashSet<>(Arrays.asList(apiPermissions.split(",")));
    }
    
    /**
     * Set 권한을 문자열로 변환하여 저장
     */
    public void setApiPermissionsSet(Set<String> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            this.apiPermissions = "";
        } else {
            this.apiPermissions = String.join(",", permissions);
        }
    }
}
