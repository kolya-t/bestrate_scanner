package io.lastwill.eventscan.events.model;

import io.lastwill.eventscan.model.NetworkType;
import io.mywish.blockchain.WrapperTransaction;
import lombok.Getter;

import java.math.BigInteger;

@Getter
public class NeoPaymentEvent extends PaymentEvent {
    private WrapperTransaction neoTransaction;
    public NeoPaymentEvent(NetworkType networkType, WrapperTransaction transaction, java.lang.String address, BigInteger amount, boolean isSuccess) {
        super(networkType, null, address, amount, String.NEO, isSuccess);
        this.neoTransaction = transaction;
    }
}
