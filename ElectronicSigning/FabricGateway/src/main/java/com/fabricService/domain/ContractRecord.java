package com.fabricService.domain;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class ContractRecord {
    private String contractID;
    private String partyA;
    private String partyB;
    private String contentHash;
    private String signTime;

    // 构造函数
    public ContractRecord(String contractID, String partyA, String partyB, String contentHash, String signTime) {
        this.contractID = contractID;
        this.partyA = partyA;
        this.partyB = partyB;
        this.contentHash = contentHash;
        this.signTime = signTime;
    }

}
