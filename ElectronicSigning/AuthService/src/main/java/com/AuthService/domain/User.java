package com.AuthService.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Table(name="users")
@Entity
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uid;
    private String uname;
    private String password;
    private String fabricCertificate;  // Fabric 证书
    private String fabricPrivateKey;   // Fabric 私钥
    private Timestamp created_at;
    @PrePersist
    public void prePersist() {
        if (this.created_at == null) {
            this.created_at = new Timestamp(System.currentTimeMillis());  // 设置当前时间戳
        }
    }
}
