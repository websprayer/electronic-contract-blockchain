package com.AuthService.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    private Algorithm signAlgorithm; // 用于签名（私钥）
    private Algorithm verifyAlgorithm; // 用于验证（公钥）

    @PostConstruct
    public void init() {
        try {
            // 加载私钥（签名用）
            String privateKeyPEM = loadKeyFromResource("private_key.pem")
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyPEM);
            PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            RSAPrivateKey privateKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(privateSpec);
            this.signAlgorithm = Algorithm.RSA256(null, privateKey);

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

    public String generateToken(Long uid, String uname) {
        if (signAlgorithm == null) {
            throw new IllegalStateException("签名算法未初始化");
        }

        return JWT.create()
                .withSubject(String.valueOf(uid))
                .withClaim("uname", uname)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 60 * 60 * 1000)) // 1小时
                .sign(signAlgorithm);
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
