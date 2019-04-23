package io.lastwill.eventscan.events.model;

import io.lastwill.eventscan.model.CryptoCurrency;
import io.lastwill.eventscan.model.NetworkType;
import io.lastwill.eventscan.model.Subscription;
import io.mywish.blockchain.WrapperTransaction;
import lombok.Getter;

import java.math.BigInteger;

@Getter
public class SubscriptionPaymentEvent extends PaymentEvent {
    private final Subscription subscription;

    public SubscriptionPaymentEvent(NetworkType networkType, WrapperTransaction transaction, BigInteger amount, CryptoCurrency currency, Subscription subscription, boolean isSuccess) {
        super(networkType, transaction, subscription.getTcrAddress(), amount, currency, isSuccess);
        this.subscription = subscription;
    }
}
