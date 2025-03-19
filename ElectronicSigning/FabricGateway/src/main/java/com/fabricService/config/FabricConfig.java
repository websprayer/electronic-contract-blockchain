package com.fabricService.config;

import io.grpc.Grpc;
import io.grpc.ManagedChannel;
import io.grpc.TlsChannelCredentials;
import org.hyperledger.fabric.client.Gateway;
import org.hyperledger.fabric.client.identity.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

@Configuration
public class FabricConfig {

    @Value("${fabric.peer-endpoint:localhost:7051}")
    private String PEER_ENDPOINT;

    @Value("${fabric.override-authority:peer0.org1.example.com}")
    private String OVERRIDE_AUTH;

    @Value("${fabric.msp-id:Org1MSP}")
    private String MSP_ID;

    @Value("${fabric.channel-name:mychannel}")
    private String CHANNEL_NAME;

    @Value("${fabric.chaincode-name:contractsigning}")
    private String CHAINCODE_NAME;

    private static final String CERT_RESOURCE_PATH = "crypto/cert.pem";
    private static final String KEY_RESOURCE_PATH = "crypto/private_key.pem";
    private static final String TLS_CERT_RESOURCE_PATH = "crypto/ca.crt";

    @Bean
    public Gateway gateway() throws Exception {
        var channel = newGrpcConnection();

        var builder = Gateway.newInstance()
                .identity(newIdentity())
                .signer(newSigner())
                .connection(channel)
                .evaluateOptions(options -> options.withDeadlineAfter(5, TimeUnit.SECONDS))
                .endorseOptions(options -> options.withDeadlineAfter(15, TimeUnit.SECONDS))
                .submitOptions(options -> options.withDeadlineAfter(5, TimeUnit.SECONDS))
                .commitStatusOptions(options -> options.withDeadlineAfter(1, TimeUnit.MINUTES));

        // 连接并返回 Gateway 实例
        return builder.connect();
    }

    private ManagedChannel newGrpcConnection() throws IOException {
        InputStream tlsCertStream = FabricConfig.class.getClassLoader().getResourceAsStream(TLS_CERT_RESOURCE_PATH);
        if (tlsCertStream == null) {
            throw new FileNotFoundException("Unable to find TLS certificate file: " + TLS_CERT_RESOURCE_PATH);
        }

        var credentials = TlsChannelCredentials.newBuilder()
                .trustManager(tlsCertStream)
                .build();

        return Grpc.newChannelBuilder(PEER_ENDPOINT, credentials)
                .overrideAuthority(OVERRIDE_AUTH)
                .build();
    }

    private Identity newIdentity() throws IOException, CertificateException {
        InputStream certStream = FabricConfig.class.getClassLoader().getResourceAsStream(CERT_RESOURCE_PATH);
        if (certStream == null) {
            throw new FileNotFoundException("Unable to find certificate file: " + CERT_RESOURCE_PATH);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(certStream))) {
            StringBuilder certString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                certString.append(line).append("\n");
            }
            var certificate = Identities.readX509Certificate(certString.toString());
            return new X509Identity(MSP_ID, certificate);
        } finally {
            certStream.close();
        }
    }

    private Signer newSigner() throws IOException, InvalidKeyException {
        InputStream keyStream = FabricConfig.class.getClassLoader().getResourceAsStream(KEY_RESOURCE_PATH);
        if (keyStream == null) {
            throw new FileNotFoundException("Unable to find private key file: " + KEY_RESOURCE_PATH);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(keyStream))) {
            StringBuilder keyString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                keyString.append(line).append("\n");
            }
            var privateKey = Identities.readPrivateKey(keyString.toString());
            return Signers.newPrivateKeySigner(privateKey);
        } finally {
            keyStream.close();
        }
    }
}
