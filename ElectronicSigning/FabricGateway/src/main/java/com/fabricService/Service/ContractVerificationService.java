package com.fabricService.Service;


import com.fabricService.domain.ContractRecord;
import com.fabricService.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ContractVerificationService {

    @Autowired
    private ContractService contractService; // 包含 readContract 方法

    @Autowired
    private UserRemoteService userRemoteService; // 包含 readPdfAndGenerateHash 方法

    public Result<String> verifyContract(
            String contractID,
            String partyA,
            String partyB,
            String signTime,
            MultipartFile file
    ) {
        try {
            System.out.println("开始验证合同...contractID: " + contractID);
            // 从区块链读取合同
            ContractRecord onChainRecord = contractService.readContract(contractID);

            // 生成文件哈希
            String uploadedFileHash = userRemoteService.getHashFromPdf(file);

            // 比对字段
            if (!onChainRecord.getContractID().equals(contractID)) {
                return Result.error("1001", "合同 ID 不匹配");
            }
            if (!onChainRecord.getPartyA().equals(partyA)) {
                return Result.error("1002", "甲方信息不匹配");
            }
            if (!onChainRecord.getPartyB().equals(partyB)) {
                return Result.error("1003", "乙方信息不匹配");
            }
            if (!onChainRecord.getSignTime().equals(signTime)) {
                return Result.error("1004", "签署时间不匹配");
            }
            if (!onChainRecord.getContentHash().equals(uploadedFileHash)) {
                return Result.error("1005", "文件哈希值不匹配，合同内容可能被篡改");
            }
            System.out.println("验证通过，所有字段匹配");

            return Result.success("合同验证通过");

        } catch (Exception e) {
            return Result.error("9999", "验证失败：" + e.getMessage());
        }
    }
}