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
    TECRA_MAINNET(NetworkProviderType.BTC),
    WAVES_MAINNET(NetworkProviderType.WAVES),
    WAVES_TESTNET(NetworkProviderType.WAVES),
    BINANCE_MAINNET(NetworkProviderType.BINANCE),
    BINANCE_TESTNET(NetworkProviderType.BINANCE),
    NEO_MAINNET(NetworkProviderType.NEO),
    NEO_TESTNET(NetworkProviderType.NEO);

    public final static String ETHEREUM_MAINNET_VALUE = "ETHEREUM_MAINNET";
    public final static String ETHEREUM_ROPSTEN_VALUE = "ETHEREUM_ROPSTEN";
    public final static String BTC_MAINNET_VALUE = "BTC_MAINNET";
    public final static String BTC_TESTNET_3_VALUE = "BTC_TESTNET_3";
    public final static String EOS_MAINNET_VALUE = "EOS_MAINNET";
    public final static String EOS_TESTNET_VALUE = "EOS_TESTNET";
    public final static String TECRA_MAINNET_VALUE = "TECRA_MAINNET";
    public final static String WAVES_MAINNET_VALUE = "WAVES_MAINNET";
    public final static String WAVES_TESTNET_VALUE = "WAVES_TESTNET";
    public final static String BINANCE_MAINNET_VALUE = "BINANCE_MAINNET";
    public final static String BINANCE_TESTNET_VALUE = "BINANCE_TESTNET";
    public final static String NEO_MAINNET_VALUE = "NEO_MAINNET";
    public final static String NEO_TESTNET_VALUE = "NEO_TESTNET";

    private final NetworkProviderType networkProviderType;

    NetworkType(NetworkProviderType networkProviderType) {
        this.networkProviderType = networkProviderType;
    }
}
