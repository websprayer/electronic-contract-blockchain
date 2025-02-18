package org.fabric.contractsigning;



import com.owlike.genson.Genson;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.*;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import org.hyperledger.fabric.shim.ledger.KeyValue;

import java.util.ArrayList;
import java.util.List;

@Contract(
        name = "contractsigning",
        info = @Info(
                title = "Smart Contract Signing",
                description = "Chaincode for electronic contract signing",
                version = "0.0.1",
                license = @License(
                        name = "Apache 2.0",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"
                ),
                contact = @Contact(
                        email = "contract@example.com",
                        name = "Chaincode Dev"
                )
        )
)
@Default
public class ContractSigning implements ContractInterface {

    private final Genson genson = new Genson();

    private enum ContractErrors {
        CONTRACT_ALREADY_EXISTS,
        CONTRACT_NOT_FOUND
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public ContractRecord CreateContract(Context ctx, String contractID, String partyA, String partyB, String contentHash, String signTime) {
        if (ContractExists(ctx, contractID)) {
            throw new ChaincodeException("Contract already exists", ContractErrors.CONTRACT_ALREADY_EXISTS.toString());
        }

        ContractRecord contract = new ContractRecord(contractID, partyA, partyB, contentHash, signTime);
        String json = genson.serialize(contract);
        ctx.getStub().putStringState(contractID, json);
        return contract;
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public ContractRecord ReadContract(Context ctx, String contractID) {
        String contractJSON = ctx.getStub().getStringState(contractID);
        if (contractJSON == null || contractJSON.isEmpty()) {
            throw new ChaincodeException("Contract not found", ContractErrors.CONTRACT_NOT_FOUND.toString());
        }

        return genson.deserialize(contractJSON, ContractRecord.class);
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public ContractRecord UpdateContract(Context ctx, String contractID, String partyA, String partyB, String contentHash, String signTime) {
        if (!ContractExists(ctx, contractID)) {
            throw new ChaincodeException("Contract not found", ContractErrors.CONTRACT_NOT_FOUND.toString());
        }

        ContractRecord updated = new ContractRecord(contractID, partyA, partyB, contentHash, signTime);
        String json = genson.serialize(updated);
        ctx.getStub().putStringState(contractID, json);
        return updated;
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void DeleteContract(Context ctx, String contractID) {
        if (!ContractExists(ctx, contractID)) {
            throw new ChaincodeException("Contract not found", ContractErrors.CONTRACT_NOT_FOUND.toString());
        }

        ctx.getStub().delState(contractID);
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean ContractExists(Context ctx, String contractID) {
        String json = ctx.getStub().getStringState(contractID);
        return (json != null && !json.isEmpty());
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public List<ContractRecord> GetAllContracts(Context ctx) {
        List<ContractRecord> contracts = new ArrayList<>();
        QueryResultsIterator<KeyValue> results = ctx.getStub().getStateByRange("", "");
        for (KeyValue result : results) {
            ContractRecord contract = genson.deserialize(result.getStringValue(), ContractRecord.class);
            contracts.add(contract);
        }
        return contracts;
    }
}
