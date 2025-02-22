package com.AuthService.domain;
import lombok.Getter;
@Getter
public class FabricIdentity {
    private String certificate;
    private String privateKey;
    public FabricIdentity(String certificate, String privateKey) {
        this.certificate = certificate;
        this.privateKey = privateKey;
    }
}
