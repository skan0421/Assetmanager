package com.assetmanager.security.oauth2;

import java.util.Map;

public record OAuth2UserInfo(Map<String, Object> attributes) {
    public String getEmail() {
        Object email = attributes.get("email");
        return email != null ? email.toString() : null;
    }
}
