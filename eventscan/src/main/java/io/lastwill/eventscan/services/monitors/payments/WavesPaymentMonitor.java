package io.lastwill.eventscan.services.monitors.payments;

import io.lastwill.eventscan.events.model.PaymentEvent;
import io.lastwill.eventscan.model.NetworkProviderType;
import io.lastwill.eventscan.model.NetworkType;
import io.lastwill.eventscan.repositories.SubscriptionRepository;
import io.mywish.blockchain.WrapperTransaction;
import io.mywish.scanner.model.NewBlockEvent;
import io.mywish.scanner.services.EventPublisher;
import io.mywish.waves.blockchain.model.WrapperOutputWaves;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class WavesPaymentMonitor {
    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private EventPublisher eventPublisher;

    @EventListener
    public void newBlockEvent(final NewBlockEvent newBlockEvent) {
        NetworkType networkType = newBlockEvent.getNetworkType();
        if (networkType.getNetworkProviderType() != NetworkProviderType.WAVES) {
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

        subscriptionRepository.findSubscribedByAddressesListAndNetwork(addresses, networkType)
                .forEach(subscription -> {
                    List<WrapperTransaction> transactions = newBlockEvent
                            .getTransactionsByAddress()
                            .get(subscription.getAddress());
                    if (transactions == null || transactions.isEmpty()) {
                        return;
                    }

                    transactions.forEach(transaction -> transaction.getOutputs()
                            .stream()
                            .map(output -> (WrapperOutputWaves) output)
                            .filter(outputWaves -> Objects.equals(subscription.getTokenAddress(), outputWaves.getAssetId()))
                            .filter(o -> subscription.getAddress().equalsIgnoreCase(o.getFrom())
                                    || subscription.getAddress().equalsIgnoreCase(o.getAddress()))
                            .forEach(output -> eventPublisher.publish(new PaymentEvent(
                                    subscription,
                                    networkType,
                                    newBlockEvent.getBlock(),
                                    transaction,
                                    output.getFrom(),
                                    output.getAddress(),
                                    output.getValue(),
                                    null,
                                    subscription.getTokenAddress(),
                                    subscription.getCurrency(),
                                    null,
                                    true
                            ))));
                });
    }
}
