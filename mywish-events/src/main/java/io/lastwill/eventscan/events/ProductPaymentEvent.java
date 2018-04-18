package io.lastwill.eventscan.events;

import io.lastwill.eventscan.model.Contract;
import io.lastwill.eventscan.model.CryptoCurrency;
import io.lastwill.eventscan.model.Product;
import io.mywish.scanner.model.NetworkType;
import lombok.Getter;
import org.bitcoinj.core.TransactionOutput;
import org.web3j.protocol.core.methods.response.Transaction;

import java.math.BigInteger;

@Getter
public class ProductPaymentEvent extends PaymentEvent {
    private final Product product;
    private final TransactionOutput transactionOutput;

    public ProductPaymentEvent(NetworkType networkType, Transaction transaction, String address, BigInteger amount, CryptoCurrency currency, boolean isSuccess, Product product, TransactionOutput transactionOutput) {
        super(networkType, transaction, address, amount, currency, isSuccess);
        this.product = product;
        this.transactionOutput = transactionOutput;
    }
}