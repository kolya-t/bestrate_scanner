package io.lastwill.eventscan.events.model;

import io.lastwill.eventscan.model.NetworkType;
import io.lastwill.eventscan.model.Subscription;
import io.mywish.blockchain.WrapperBlock;
import io.mywish.blockchain.WrapperTransaction;
import lombok.Getter;

import java.math.BigInteger;

@Getter
public class PaymentEvent extends BaseEvent {
    private final Subscription subscription;
    private final WrapperBlock block;
    private final WrapperTransaction transaction;
    private final String addressFrom;
    private final String addressTo;
    private final BigInteger amount;
    private final BigInteger fee;
    private final String tokenAddress;
    private final String currency;
    private final String memo;
    private final boolean isSuccess;

    public PaymentEvent(Subscription subscription,
                        NetworkType networkType,
                        WrapperBlock block,
                        WrapperTransaction transaction,
                        String addressFrom,
                        String addressTo,
                        BigInteger amount,
                        BigInteger fee,
                        String tokenAddress,
                        String currency,
                        String memo,
                        boolean isSuccess) {
        super(networkType);
        this.subscription = subscription;
        this.block = block;
        this.transaction = transaction;
        this.addressFrom = addressFrom;
        this.addressTo = addressTo;
        this.amount = amount;
        this.fee = fee;
        this.tokenAddress = tokenAddress;
        this.currency = currency;
        this.memo = memo;
        this.isSuccess = isSuccess;
    }
}
