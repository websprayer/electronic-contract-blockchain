//package com.AuthService.repository;
//import io.grpc.ManagedChannel;
//import io.grpc.Grpc;
//import io.grpc.ChannelCredentials;
//import io.grpc.TlsChannelCredentials;
//import org.hyperledger.fabric.client.*;
//import org.hyperledger.fabric.client.identity.*;
//import org.hyperledger.fabric.gateway.Gateway;
//
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.security.PrivateKey;
//import java.security.cert.X509Certificate;
//import java.util.concurrent.TimeUnit;
//
//public class FabricContractDAO {
//    private static final String MSP_ID = "Org1MSP";  // MSP ID
//    private static final String CERT_PATH = "path/to/certificate.pem";  // 用户证书路径
//    private static final String PRIVATE_KEY_PATH = "path/to/private-key.pem";  // 私钥路径
//    private static final String TLS_CERT_PATH = "path/to/tls-CA-certificate.pem";  // TLS 证书
//    private static final String GATEWAY_URL = "gateway.example.org:1337";  // Fabric Gateway 地址
//    private static final String CHANNEL_NAME = "mychannel";  // Fabric 通道名
//    private static final String CHAINCODE_NAME = "contractCC";  // 智能合约名
//
//    private Gateway gateway;
//    private Contract contract;
//    private ManagedChannel grpcChannel;
//
//    public FabricContractDAO() throws Exception {
//        initializeConnection();
//    }
//
//    /**
//     * 初始化 Fabric 连接
//     */
//    private void initializeConnection() throws Exception {
//        // 读取用户证书
//        X509Certificate certificate = Identities.readX509Certificate(Files.newBufferedReader(Paths.get(CERT_PATH)));
//        Identity identity = new X509Identity(MSP_ID, certificate);
//
//        // 读取用户私钥
//        PrivateKey privateKey = Identities.readPrivateKey(Files.newBufferedReader(Paths.get(PRIVATE_KEY_PATH)));
//        Signer signer = Signers.newPrivateKeySigner(privateKey);
//
//        // 创建 gRPC 连接
//        ChannelCredentials tlsCredentials = TlsChannelCredentials.newBuilder()
//                .trustManager(Paths.get(TLS_CERT_PATH).toFile())
//                .build();
//        grpcChannel = Grpc.newChannelBuilder(GATEWAY_URL, tlsCredentials).build();
//
//        // 创建 Gateway 连接
//        Gateway.Builder builder = Gateway.newInstance()
//                .identity(identity)
//                .signer(signer)
//                .connection(grpcChannel);
//        gateway = builder.connect();
//
//        // 获取智能合约
//        Network network = gateway.getNetwork(CHANNEL_NAME);
//        contract = network.getContract(CHAINCODE_NAME);
//    }
//
//    /**
//     * 存储电子合同
//     */
//    public void createContract(String contractId, String content, String owner) throws Exception {
//        byte[] result = contract.submitTransaction("createContract", contractId, content, owner);
//        System.out.println("合约创建成功: " + new String(result, StandardCharsets.UTF_8));
//    }
//
//    /**
//     * 查询电子合同
//     */
//    public String queryContract(String contractId) throws Exception {
//        byte[] result = contract.evaluateTransaction("queryContract", contractId);
//        return new String(result, StandardCharsets.UTF_8);
//    }
//
//    /**
//     * 更新电子合同
//     */
//    public void updateContract(String contractId, String newContent) throws Exception {
//        byte[] result = contract.submitTransaction("updateContract", contractId, newContent);
//        System.out.println("合约更新成功: " + new String(result, StandardCharsets.UTF_8));
//    }
//
//    /**
//     * 删除电子合同
//     */
//    public void deleteContract(String contractId) throws Exception {
//        byte[] result = contract.submitTransaction("deleteContract", contractId);
//        System.out.println("合约删除成功: " + new String(result, StandardCharsets.UTF_8));
//    }
//
//    /**
//     * 关闭连接
//     */
//    public void close() throws InterruptedException {
//        if (gateway != null) {
//            gateway.close();
//        }
//        if (grpcChannel != null) {
//            grpcChannel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
//        }
//    }
//}
//
