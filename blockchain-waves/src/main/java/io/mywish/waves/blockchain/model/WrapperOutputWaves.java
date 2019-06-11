package io.mywish.waves.blockchain.model;

import io.mywish.blockchain.WrapperOutput;
import lombok.Getter;

import java.math.BigInteger;

@Getter
public class WrapperOutputWaves extends WrapperOutput {
    private final String from;
    private final String assetId;

    public WrapperOutputWaves(String parentTransaction, String from, String to, BigInteger value, String assetId) {
        super(parentTransaction, 0, to, value, new byte[0]);
        this.assetId = assetId;
        this.from = from;
    }
}
