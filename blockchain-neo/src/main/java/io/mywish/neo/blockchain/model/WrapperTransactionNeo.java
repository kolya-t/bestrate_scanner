package io.mywish.neo.blockchain.model;

import io.mywish.blockchain.WrapperInput;
import io.mywish.blockchain.WrapperOutput;
import io.mywish.blockchain.WrapperTransaction;
import io.mywish.neocli4j.Transaction;
import lombok.Getter;

import java.math.BigInteger;
import java.util.List;

@Getter
public class WrapperTransactionNeo extends WrapperTransaction {
    private final Transaction.Type type;
    private final List<String> contracts;

    public WrapperTransactionNeo(String hash, List<WrapperInput> inputs, List<WrapperOutput> outputs, boolean contractCreation, Transaction.Type type, List<String> contracts, BigInteger fee) {
        super(hash, inputs, outputs, contractCreation, null, fee);
        this.type = type;
        this.contracts = contracts;
    }
}
