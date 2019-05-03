package io.lastwill.eventscan.events.model;

import io.lastwill.eventscan.model.NetworkType;
import io.mywish.blockchain.WrapperTransaction;
import lombok.Getter;

import java.math.BigInteger;

@Getter
public abstract class PaymentEvent extends BaseEvent {
    private final WrapperTransaction transaction;
    private final String address;
    private final BigInteger amount;
    private final String tokenAddress;
    private final String currency;
    private final boolean isSuccess;

    public PaymentEvent(NetworkType networkType, WrapperTransaction transaction, String address, BigInteger amount, String tokenAddress, String currency, boolean isSuccess) {
        super(networkType);
        this.transaction = transaction;
        this.address = address;
        this.amount = amount;
        this.tokenAddress = tokenAddress;
        this.currency = currency;
        this.isSuccess = isSuccess;
    }
}
