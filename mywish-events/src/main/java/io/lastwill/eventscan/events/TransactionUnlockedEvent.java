package io.lastwill.eventscan.events;

import io.lastwill.eventscan.model.AddressLock;
import io.lastwill.eventscan.model.BaseEvent;
import io.mywish.wrapper.WrapperTransaction;
import io.mywish.wrapper.WrapperTransactionReceipt;
import io.lastwill.eventscan.model.NetworkType;
import lombok.Getter;

@Getter
public class TransactionUnlockedEvent extends BaseEvent {
    private final AddressLock addressLock;
    private final WrapperTransaction transaction;
    private final WrapperTransactionReceipt transactionReceipt;

    public TransactionUnlockedEvent(NetworkType networkType, AddressLock addressLock, WrapperTransaction transaction, WrapperTransactionReceipt transactionReceipt) {
        super(networkType);
        this.addressLock = addressLock;
        this.transaction = transaction;
        this.transactionReceipt = transactionReceipt;
    }
}
