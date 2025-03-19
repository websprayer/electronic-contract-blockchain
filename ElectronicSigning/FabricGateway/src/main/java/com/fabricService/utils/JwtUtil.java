package com.fabricService.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class JwtUtil {

    private Algorithm verifyAlgorithm; // 用于验证（公钥）

    @Autowired
    private HttpServletRequest request;

    @PostConstruct
    public void init() {
        try {
            // 加载私钥（签名用）

            // 加载公钥（验签用）
            String publicKeyPEM = loadKeyFromResource("public_key.pem")
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyPEM);
            X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(publicKeyBytes);
            RSAPublicKey publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(publicSpec);
            this.verifyAlgorithm = Algorithm.RSA256(publicKey, null);

            System.out.println(" JWT 加密和验签算法初始化成功");
        } catch (Exception e) {
            System.err.println("JwtUtil 初始化失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
    public String getToken() {
        // 从请求的 Authorization header 获取 token
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7); // 获取 token 部分
        }
        return null; // 如果没有 token，则返回 null
    }



    public DecodedJWT verifyToken(String token) {
        try {
            if (verifyAlgorithm == null) {
                throw new IllegalStateException("验签算法未初始化");
            }
            JWTVerifier verifier = JWT.require(verifyAlgorithm).build();
            return verifier.verify(token); // 验证签名并返回结果
        } catch (Exception e) {
            System.err.println(" Token 验证失败：" + e.getMessage());
            return null;
        }
    }

    public Long extractUid(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token); // 只是解码，不验证
            return Long.parseLong(jwt.getSubject());
        } catch (Exception e) {
            return null;
        }
    }

    public String extractUname(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim("uname").asString();
        } catch (Exception e) {
            return null;
        }
    }

    private String loadKeyFromResource(String filename) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
        if (is == null) {
            throw new RuntimeException("找不到密钥文件: " + filename);
        }
        return new String(is.readAllBytes());
    }
}
