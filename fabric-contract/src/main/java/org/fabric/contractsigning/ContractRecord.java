package org.fabric.contractsigning;

import java.util.Objects;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import com.owlike.genson.annotation.JsonProperty;

@DataType()
public final class ContractRecord {

    @Property()
    private final String contractID;

    @Property()
    private final String partyA;

    @Property()
    private final String partyB;

    @Property()
    private final String contentHash;

    @Property()
    private final String signTime;

    public ContractRecord(@JsonProperty("contractID") final String contractID,
                          @JsonProperty("partyA") final String partyA,
                          @JsonProperty("partyB") final String partyB,
                          @JsonProperty("contentHash") final String contentHash,
                          @JsonProperty("signTime") final String signTime) {
        this.contractID = contractID;
        this.partyA = partyA;
        this.partyB = partyB;
        this.contentHash = contentHash;
        this.signTime = signTime;
    }

    public String getContractID() {
        return contractID;
    }
    public String getPartyA() {
        return partyA;
    }
    public String getPartyB() {
        return partyB;
    }
    public String getContentHash() {
        return contentHash;
    }
    public String getSignTime() {
        return signTime;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj){
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        ContractRecord other = (ContractRecord) obj;
        return Objects.equals(contractID, other.contractID)
                && Objects.equals(partyA, other.partyA)
                && Objects.equals(partyB, other.partyB)
                && Objects.equals(contentHash, other.contentHash)
                && Objects.equals(signTime, other.signTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contractID, partyA, partyB, contentHash, signTime);
    }

    @Override
    public String toString() {
        return "ContractRecord{" +
                "contractID='" + contractID + '\'' +
                ", partyA='" + partyA + '\'' +
                ", partyB='" + partyB + '\'' +
                ", contentHash='" + contentHash + '\'' +
                ", signTime='" + signTime + '\'' +
                '}';
    }
}
