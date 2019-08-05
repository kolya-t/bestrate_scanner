package io.lastwill.eventscan.services.monitors.payments;

import io.lastwill.eventscan.events.model.PaymentEvent;
import io.lastwill.eventscan.model.NetworkProviderType;
import io.lastwill.eventscan.model.NetworkType;
import io.lastwill.eventscan.repositories.SubscriptionRepository;
import io.mywish.binance.blockchain.model.WrapperOutputBinance;
import io.mywish.blockchain.WrapperTransaction;
import io.mywish.scanner.model.NewBlockEvent;
import io.mywish.scanner.services.EventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class BinancePaymentMonitor {
    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private EventPublisher eventPublisher;

    @EventListener
    public void onNewBlockEvent(final NewBlockEvent newBlockEvent) {
        NetworkType networkType = newBlockEvent.getNetworkType();
        if (networkType.getNetworkProviderType() != NetworkProviderType.BINANCE) {
            return;
        }

        Set<String> addresses = newBlockEvent.getTransactionsByAddress().keySet();
        if (addresses.isEmpty()) {
            return;
        }

        subscriptionRepository.findSubscribedByAddressesListAndNetwork(addresses, networkType)
                .forEach(subscription -> {
                    List<WrapperTransaction> transactions = newBlockEvent
                            .getTransactionsByAddress()
                            .get(subscription.getAddress());

                    transactions.forEach(transaction -> {
                        String currency = subscription.getCurrency();
                        transaction.getOutputs()
                                .stream()
                                .map(wrapperOutput -> (WrapperOutputBinance) wrapperOutput)
                                .filter(output -> output.getSymbol().equalsIgnoreCase(currency))
                                .forEach(output -> {
                                    String from = output.getFrom();
                                    String to = output.getAddress();
                                    String tokenAddress = subscription.getTokenAddress();
                                    BigInteger amount = output.getValue();
                                    String memo = output.getMemo();

                                    eventPublisher.publish(new PaymentEvent(
                                            subscription,
                                            networkType,
                                            newBlockEvent.getBlock(),
                                            transaction,
                                            from,
                                            to,
                                            amount,
                                            null,
                                            tokenAddress,
                                            currency,
                                            memo,
                                            true
                                    ));
                                });
                    });
                });
    }
}
