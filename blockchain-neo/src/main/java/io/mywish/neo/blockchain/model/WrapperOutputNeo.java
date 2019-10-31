package io.mywish.neo.blockchain.model;

import io.mywish.blockchain.WrapperOutput;
import lombok.Getter;

import java.math.BigInteger;

@Getter
public class WrapperOutputNeo extends WrapperOutput {
    private final Asset asset;

    public WrapperOutputNeo(String parentTransaction, Integer index, String address, BigInteger value, Asset asset, byte[] rawOutputScript) {
        super(parentTransaction, index, address, value, rawOutputScript);
        this.asset = asset;
    }
}
