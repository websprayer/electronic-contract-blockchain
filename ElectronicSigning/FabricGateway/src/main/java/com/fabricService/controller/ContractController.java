package com.fabricService.controller;

import com.fabricService.Service.ContractService;
import com.fabricService.Service.ContractVerificationService;
import com.fabricService.domain.ContractRecord;
import com.fabricService.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
//@FeignClient(name = "fabric-client-service") // nacos中注册的服务名
@RestController
@RequestMapping("/api/contracts")
public class ContractController {

    @Autowired
    private ContractService contractService; // 注入服务层（执行链码操作）

    @Autowired
    private ContractVerificationService contractVerificationService;


    @PostMapping(value = "/verify", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<String> verifyContract(
            @RequestParam("contractID") String contractID,
            @RequestParam("partyA") String partyA,
            @RequestParam("partyB") String partyB,
            @RequestParam("signTime") String signTime,
            @RequestPart("file") MultipartFile file
    ) {
        return contractVerificationService.verifyContract(contractID, partyA, partyB, signTime, file);
    }
    // 初始化账本
    @PostMapping("/initLedger")
    public String initLedger() {
        try {
            contractService.initLedger();
            return "Ledger initialized successfully.";
        } catch (Exception e) {
            return "Error initializing ledger: " + e.getMessage();
        }
    }

    // 创建合约
    @PostMapping("/create")
    public ContractRecord createContract(
            @RequestParam String partyA,
            @RequestParam String partyB,
            @RequestParam String contentHash) {
        try {
            return contractService.createContract( partyA, partyB, contentHash);
        } catch (Exception e) {
            throw new RuntimeException("Error creating contract: " + e.getMessage());
        }
    }

    // 查询合约
    @GetMapping("/read/{contractID}")
    public ContractRecord readContract(@PathVariable String contractID) {
        try {
            return contractService.readContract(contractID);
        } catch (Exception e) {
            throw new RuntimeException("Error reading contract: " + e.getMessage());
        }
    }

    // 更新合约
    @PutMapping("/update")
    public ContractRecord updateContract(
            @RequestParam String contractID,
            @RequestParam String partyA,
            @RequestParam String partyB,
            @RequestParam String contentHash,
            @RequestParam String signTime) {
        try {
            return contractService.updateContract(contractID, partyA, partyB, contentHash, signTime);
        } catch (Exception e) {
            throw new RuntimeException("Error updating contract: " + e.getMessage());
        }
    }

    // 删除合约
    @DeleteMapping("/delete/{contractID}")
    public String deleteContract(@PathVariable String contractID) {
        try {
            contractService.deleteContract(contractID);
            return "Contract deleted successfully.";
        } catch (Exception e) {
            return "Error deleting contract: " + e.getMessage();
        }
    }

    // 查询合约是否存在
    @GetMapping("/exists/{contractID}")
    public boolean contractExists(@PathVariable String contractID) {
        try {
            return contractService.contractExists(contractID);
        } catch (Exception e) {
            throw new RuntimeException("Error checking if contract exists: " + e.getMessage());
        }
    }

    // 获取所有合约
    @GetMapping("/all")
    public List<ContractRecord> getAllContracts() {
        try {
            return contractService.getAllContracts();
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving all contracts: " + e.getMessage());
        }
    }
}

