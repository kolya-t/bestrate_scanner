package io.mywish.tron.blockchain.model;

import io.mywish.blockchain.WrapperInput;
import io.mywish.blockchain.WrapperOutput;
import io.mywish.blockchain.WrapperTransaction;
import lombok.Getter;

import java.math.BigInteger;
import java.util.List;

@Getter
public class WrapperTransactionTron extends WrapperTransaction {
    private final List<String> contracts;
    private final String status;

    public WrapperTransactionTron(String hash, List<WrapperInput> inputs, List<WrapperOutput> outputs, boolean contractCreation, List<String> contracts, String transitionStatus, Long fee) {
        super(hash, inputs, outputs, contractCreation, null, BigInteger.valueOf(fee));
        this.contracts = contracts;
        this.status = transitionStatus;
    }
}
