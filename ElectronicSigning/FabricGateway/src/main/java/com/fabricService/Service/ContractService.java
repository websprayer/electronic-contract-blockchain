package com.fabricService.Service;

import com.fabricService.domain.ContractRecord;
import com.fabricService.repository.UserContractRepository;
import com.google.gson.Gson;
import jakarta.transaction.Transactional;
import org.hyperledger.fabric.client.Contract;
import org.hyperledger.fabric.client.Gateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

@Service
public class ContractService {

    private static final String CHANNEL_NAME = "mychannel";
    private static final String CHAINCODE_NAME = "contractsigning";

    private Contract contract;

    private ContractRecord contractRecord;
    private final Gson gson = new Gson();
    private String contractId = "contract2";

    @Autowired
    private UserRemoteService userRemoteService;


    public ContractService(final Gateway gateway) {
        var network = gateway.getNetwork(CHANNEL_NAME);
        contract = network.getContract(CHAINCODE_NAME);
    }



    public void initLedger() throws Exception {
        System.out.println("Initializing ledger...");
        contract.submitTransaction("InitLedger");
        System.out.println("Ledger initialized successfully.");
    }
    @Autowired
    private UserContractRepository contractRepository;
    // 创建合约 (CreateContract)

    @Transactional
    public ContractRecord createContract( String partyA,String partyB, String contentHash) throws Exception {
        System.out.println("\nCreating contract...");
        long lgpartyA = userRemoteService.getUidByUname(partyA);
        long lgpartyB = userRemoteService.getUidByUname(partyB);
        System.out.println("转换后的 Party A UID: " + lgpartyA);
        System.out.println("转换后的 Party B UID: " + lgpartyB + "\n");

        contractRepository.insertContract(lgpartyA, lgpartyB);
        System.out.println("已插入数据库中的合同基础信息");

        Long contractId = contractRepository.getLastContractId();
        System.out.println("获取到的合同ID: " + contractId + "\n");
        String contractIdStr = String.valueOf(contractId);
        String PartyAStr = String.valueOf(partyA);
        String PartyBStr = String.valueOf(partyB);
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String signTime = now.format(formatter);
        ContractRecord contractRecord = new ContractRecord(contractIdStr, PartyAStr, PartyBStr, contentHash, signTime);
        System.out.println("准备上链的数据:");
        System.out.println("contractId: " + contractIdStr);
        System.out.println("partyA: " + PartyAStr);
        System.out.println("partyB: " + PartyBStr);

        contract.submitTransaction("CreateContract", contractIdStr, PartyAStr, PartyBStr, contentHash, signTime);
        System.out.println("已提交合约到区块链\n");

        System.out.println("========== 合同创建完成 ==========\n");

        return contractRecord;
    }

    // 查询合约 (ReadContract)
    public ContractRecord readContract(String contractID) throws Exception {
        System.out.println("Reading contract...");
        byte[] result = contract.evaluateTransaction("ReadContract", contractID);
        String contractJson = new String(result);
        if (contractJson == null || contractJson.isEmpty()) {
            System.out.println("合约不存在");
            throw new RuntimeException("Contract not found");
        }
        return new Gson().fromJson(contractJson, ContractRecord.class);
    }

    // 更新合约 (UpdateContract)
    public ContractRecord updateContract(String contractID, String partyA, String partyB, String contentHash, String signTime) throws Exception {
        System.out.println("Updating contract...");
        ContractRecord updatedContract = new ContractRecord(contractID, partyA, partyB, contentHash, signTime);
        contract.submitTransaction("UpdateContract", contractID, partyA, partyB, contentHash, signTime);
        return updatedContract;
    }

    // 删除合约 (DeleteContract)
    public void deleteContract(String contractID) throws Exception {
        System.out.println("Deleting contract...");
        long contractIDLg = Long.parseLong(contractID);
        contractRepository.deleteByContractId(contractIDLg);
        contract.submitTransaction("DeleteContract", contractID);
    }

    // 查询是否存在合约 (ContractExists)
    public boolean contractExists(String contractID) throws Exception {
        System.out.println("Checking if contract exists...");
        byte[] result = contract.evaluateTransaction("ContractExists", contractID);
        return Boolean.parseBoolean(new String(result));
    }

    // 获取所有合约 (GetAllContracts)
    public List<ContractRecord> getAllContracts() throws Exception {
        System.out.println("Getting all contracts...");
        List<ContractRecord> contracts = new ArrayList<>();
        byte[] result = contract.evaluateTransaction("GetAllContracts");
        String resultJson = new String(result);
        if (resultJson != null && !resultJson.isEmpty()) {
            ContractRecord[] contractArray = new Gson().fromJson(resultJson, ContractRecord[].class);
            for (ContractRecord contractRecord : contractArray) {
                contracts.add(contractRecord);
            }
        }
        return contracts;
    }
}

