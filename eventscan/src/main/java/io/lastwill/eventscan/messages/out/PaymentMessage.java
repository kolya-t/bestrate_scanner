package io.lastwill.eventscan.messages.out;

import io.lastwill.eventscan.model.NetworkType;
import io.lastwill.eventscan.model.Subscription;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigInteger;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
public class PaymentMessage extends BaseOutMessage {
    private NetworkType network;
    private String transactionHash;
    private String tokenAddress;
    private String currency;
    private BigInteger amount;

    public PaymentMessage(Subscription subscription, NetworkType network, String transactionHash, String tokenAddress, String currency, BigInteger amount) {
        super(subscription);
        this.network = network;
        this.transactionHash = transactionHash;
        this.tokenAddress = tokenAddress;
        this.currency = currency;
        this.amount = amount;
    }
}
