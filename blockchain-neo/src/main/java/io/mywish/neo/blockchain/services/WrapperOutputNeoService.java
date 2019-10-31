package io.mywish.neo.blockchain.services;

import io.mywish.neo.blockchain.model.Asset;
import io.mywish.neo.blockchain.model.WrapperOutputNeo;
import io.mywish.neocli4j.Transaction;
import io.mywish.neocli4j.TransactionOutput;
import io.mywish.blockchain.WrapperOutput;
import org.springframework.stereotype.Component;

@Component
public class WrapperOutputNeoService {
    public WrapperOutputNeo build(Transaction transaction, TransactionOutput output) {
        return new WrapperOutputNeo(
                transaction.getHash(),
                output.getIndex(),
                output.getAddress(),
                output.getValue(),
                Asset.getAssetById(output.getAsset()),
                new byte[0]);
    }
}
