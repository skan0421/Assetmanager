package com.assetmanager.controller;

import com.assetmanager.domain.User;
import com.assetmanager.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 테스트용 컨트롤러
 * Phase 2.3: MyBatis 연결 테스트
 */
@RestController
@RequestMapping("/api/test")
public class TestController {
    
    @Autowired(required = false)
    private UserMapper userMapper;
    
    /**
     * 애플리케이션 상태 확인
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", System.currentTimeMillis());
        status.put("message", "Asset Manager Application is running");
        return status;
    }
    
    /**
     * 데이터베이스 연결 테스트
     */
    @GetMapping("/db")
    public Map<String, Object> testDatabase() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (userMapper != null) {
                int userCount = userMapper.countActiveUsers();
                List<User> users = userMapper.findAll();
                
                result.put("status", "SUCCESS");
                result.put("activeUserCount", userCount);
                result.put("totalUsers", users.size());
                result.put("users", users);
            } else {
                result.put("status", "ERROR");
                result.put("message", "UserMapper is not available");
            }
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("message", e.getMessage());
        }
        
        return result;
    }
}
