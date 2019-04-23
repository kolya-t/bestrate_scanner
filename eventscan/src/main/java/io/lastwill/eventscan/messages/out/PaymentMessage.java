package io.lastwill.eventscan.messages.out;

import io.lastwill.eventscan.model.CryptoCurrency;
import io.lastwill.eventscan.model.NetworkType;
import io.lastwill.eventscan.model.Subscription;
import lombok.Getter;
import lombok.ToString;

import java.math.BigInteger;

@Getter
@ToString(callSuper = true)
public class PaymentMessage extends BaseOutMessage {
    private final String type = "payment";
    private final NetworkType network;
    private final String transactionHash;
    private final CryptoCurrency currency;
    private final BigInteger amount;

    public PaymentMessage(Subscription subscription, NetworkType network, String transactionHash, CryptoCurrency currency, BigInteger amount) {
        super(subscription);
        this.network = network;
        this.transactionHash = transactionHash;
        this.currency = currency;
        this.amount = amount;
    }
}
