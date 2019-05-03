package io.mywish.blockchain;

import lombok.Getter;

import java.math.BigInteger;

@Getter
public class WrapperInput {
    private final String parentTransaction;
    private final String address;
//    private final Integer vIndex;
//    private final Integer oIndex;
    private final BigInteger value;
//    private final byte[] rawOutputScript;

    public WrapperInput(String parentTransaction, String address, BigInteger value) {
        this.parentTransaction = parentTransaction;
        this.address = address;
        this.value = value;
    }
}
