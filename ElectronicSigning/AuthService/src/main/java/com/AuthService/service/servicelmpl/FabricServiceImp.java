package com.AuthService.service.servicelmpl;

import com.AuthService.domain.FabricIdentity;
import com.AuthService.service.FabricService;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Date;

@Service
public class FabricServiceImp implements FabricService {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Override
    public FabricIdentity registerUser(String uname, String password) {
        try {
            // 生成 EC P-256 密钥对（Hyperledger Fabric 要求的曲线）
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC", "BC");
            keyGen.initialize(256, new SecureRandom());
            KeyPair keyPair = keyGen.generateKeyPair();

            // 构建自签名 X.509 证书
            X500Name subject = new X500Name("CN=" + uname + ",O=Org1MSP,C=US");
            BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());
            Date notBefore = new Date();
            Date notAfter = new Date(notBefore.getTime() + 365L * 24 * 60 * 60 * 1000); // 1年有效期

            ContentSigner signer = new JcaContentSignerBuilder("SHA256withECDSA")
                    .setProvider("BC")
                    .build(keyPair.getPrivate());

            X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                    subject, serial, notBefore, notAfter, subject, keyPair.getPublic());

            X509Certificate certificate = new JcaX509CertificateConverter()
                    .setProvider("BC")
                    .getCertificate(certBuilder.build(signer));

            // PEM 编码
            String certPem = toPem(certificate);
            String privateKeyPem = toPem(keyPair.getPrivate());

            return new FabricIdentity(certPem, privateKeyPem);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Fabric identity for user: " + uname, e);
        }
    }

    private String toPem(Object obj) throws Exception {
        StringWriter sw = new StringWriter();
        try (JcaPEMWriter writer = new JcaPEMWriter(sw)) {
            writer.writeObject(obj);
        }
        return sw.toString();
    }
}
