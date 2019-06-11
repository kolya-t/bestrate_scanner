package io.mywish.waves.blockchain.model;

import io.mywish.blockchain.WrapperInput;
import io.mywish.blockchain.WrapperOutput;
import io.mywish.blockchain.WrapperTransaction;
import lombok.Getter;

import java.math.BigInteger;
import java.util.List;

@Getter
public class WrapperTransactionWaves extends WrapperTransaction {
    public WrapperTransactionWaves(String hash, List<WrapperInput> inputs, List<WrapperOutput> outputs, BigInteger fee) {
        super(hash, inputs, outputs, false, BigInteger.ZERO, fee);
    }
}
