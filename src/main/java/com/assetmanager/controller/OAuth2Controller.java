package com.assetmanager.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/oauth2")
public class OAuth2Controller {
    @GetMapping("/callback")
    public String callback() {
        return "oauth2 callback";
    }
}
