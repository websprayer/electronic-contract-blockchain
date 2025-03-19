package com.fabricService.config;

import com.fabricService.utils.JwtUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FeignClientInterceptor implements RequestInterceptor {

    @Autowired
    private JwtUtil jwtUtil; // 用于处理JWT的工具类

    @Override
    public void apply(RequestTemplate template) {
        // 获取当前请求的JWT令牌
        String token = jwtUtil.getToken();
        if (token != null) {
            template.header("Authorization", "Bearer " + token);
        }
    }
}
