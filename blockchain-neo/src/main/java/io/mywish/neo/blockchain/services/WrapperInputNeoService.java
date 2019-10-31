package io.mywish.neo.blockchain.services;

import io.mywish.blockchain.WrapperInput;
import io.mywish.neo.blockchain.model.Asset;
import io.mywish.neo.blockchain.model.WrapperInputNeo;
import io.mywish.neocli4j.Transaction;
import io.mywish.neocli4j.TransactionInput;
import io.mywish.neocli4j.TransactionOutput;
import org.springframework.stereotype.Component;

@Component
public class WrapperInputNeoService {
    public WrapperInputNeo build(Transaction transaction, TransactionInput input) {
        TransactionOutput connectedOutput = input.getConnectedOutput();
        return new WrapperInputNeo(
                transaction.getHash(),
                connectedOutput.getAddress(),
                connectedOutput.getValue(),
                Asset.getAssetById(connectedOutput.getAsset())
        );
    }
}
