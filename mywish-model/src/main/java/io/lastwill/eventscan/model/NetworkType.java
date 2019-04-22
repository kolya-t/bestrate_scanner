package io.lastwill.eventscan.model;

import lombok.Getter;

@Getter
public enum NetworkType {
    TECRA_MAINNET(NetworkProviderType.TECRA);

    public final static String TECRA_MAINNET_VALUE = "TECRA_MAINNET";

    private final NetworkProviderType networkProviderType;

    NetworkType(NetworkProviderType networkProviderType) {
        this.networkProviderType = networkProviderType;
    }
}
