package io.lastwill.eventscan.services.monitors.payments;

import io.lastwill.eventscan.events.model.PaymentEvent;
import io.lastwill.eventscan.model.NetworkProviderType;
import io.lastwill.eventscan.model.NetworkType;
import io.lastwill.eventscan.model.Subscription;
import io.lastwill.eventscan.repositories.SubscriptionRepository;
import io.lastwill.eventscan.services.AddressConverter;
import io.mywish.blockchain.WrapperTransaction;
import io.mywish.scanner.model.NewBlockEvent;
import io.mywish.scanner.services.EventPublisher;
import io.mywish.tron.blockchain.model.WrapperOutputTron;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TronPaymentMonitor {
    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private EventPublisher eventPublisher;

    @EventListener
    public void newBlockEvent(final NewBlockEvent newBlockEvent) {
        NetworkType networkType = newBlockEvent.getNetworkType();
        if (networkType.getNetworkProviderType() != NetworkProviderType.TRON) {
            return;
        }
        Set<String> addresses = newBlockEvent
                .getTransactionsByAddress()
                .keySet()
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        if (addresses.isEmpty()) {
            return;
        }
        subscriptionRepository.findSubscribedByNetwork(networkType)
                .forEach(subscription -> {
                    String address = convertAddress(subscription.getAddress());
                    List<WrapperTransaction> transactions = newBlockEvent
                            .getTransactionsByAddress()
                            .get(address);
                    if (transactions == null || transactions.isEmpty()) {
                        return;
                    }

                    transactions.forEach(transaction -> transaction.getOutputs()
                            .stream()
                            .map(output -> (WrapperOutputTron) output)
                            .forEach(output -> {
                                String fromAddress = transaction.getInputs().get(0).getAddress();
                                String toAddress = output.getAddress();

                                if (fromAddress.equalsIgnoreCase(address)) {
                                    fromAddress = subscription.getAddress();
                                }
                                if (toAddress.equalsIgnoreCase(address)) {
                                    toAddress = subscription.getAddress();
                                }
                                eventPublisher.publish(new PaymentEvent(
                                        subscription,
                                        networkType,
                                        newBlockEvent.getBlock(),
                                        transaction,
                                        fromAddress,
                                        toAddress,
                                        output.getValue(),
                                        null,
                                        subscription.getTokenAddress(),
                                        subscription.getCurrency(),
                                        null,
                                        true
                                ));
                            }));
                });
    }

    private String convertAddress(String address) {
        String result = address;
        if (!address.substring(2).equalsIgnoreCase("41")) {
            result = AddressConverter.toTronPubKeyFrom(address);
        }
        return result;
    }
}
