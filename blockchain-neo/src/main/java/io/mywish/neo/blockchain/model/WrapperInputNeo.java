package io.mywish.neo.blockchain.model;

import io.mywish.blockchain.WrapperInput;
import lombok.Getter;

import java.math.BigInteger;

@Getter
public class WrapperInputNeo extends WrapperInput {
    private final Asset asset;

    public WrapperInputNeo(String parentTransaction, String address, BigInteger value, Asset asset) {
        super(parentTransaction, address, value);
        this.asset = asset;
    }
}
