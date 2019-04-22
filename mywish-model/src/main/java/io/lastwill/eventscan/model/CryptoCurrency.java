package io.lastwill.eventscan.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CryptoCurrency {
    TCR(8);

    @Getter
    private final int decimals;
}
