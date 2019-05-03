package io.lastwill.eventscan.model;

import lombok.Getter;

@Getter
public enum NetworkType {
    ETHEREUM_MAINNET(NetworkProviderType.WEB3),
    ETHEREUM_ROPSTEN(NetworkProviderType.WEB3),
    BTC_MAINNET(NetworkProviderType.BTC),
    BTC_TESTNET_3(NetworkProviderType.BTC),
    EOS_MAINNET(NetworkProviderType.EOS),
    EOS_TESTNET(NetworkProviderType.EOS),
    TECRA_MAINNET(NetworkProviderType.BTC);

    public final static String ETHEREUM_MAINNET_VALUE = "ETHEREUM_MAINNET";
    public final static String ETHEREUM_ROPSTEN_VALUE = "ETHEREUM_ROPSTEN";
    public final static String BTC_MAINNET_VALUE = "BTC_MAINNET";
    public final static String BTC_TESTNET_3_VALUE = "BTC_TESTNET_3";
    public final static String EOS_MAINNET_VALUE = "EOS_MAINNET";
    public final static String EOS_TESTNET_VALUE = "EOS_TESTNET";
    public final static String TECRA_MAINNET_VALUE = "TECRA_MAINNET";

    private final NetworkProviderType networkProviderType;

    NetworkType(NetworkProviderType networkProviderType) {
        this.networkProviderType = networkProviderType;
    }
}
