package io.lastwill.eventscan.services.monitors.payments;

import io.lastwill.eventscan.events.model.PaymentEvent;
import io.lastwill.eventscan.model.NetworkProviderType;
import io.lastwill.eventscan.repositories.SubscriptionRepository;
import io.mywish.blockchain.WrapperTransaction;
import io.mywish.neo.blockchain.model.WrapperInputNeo;
import io.mywish.neo.blockchain.model.WrapperOutputNeo;
import io.mywish.scanner.model.NewBlockEvent;
import io.mywish.scanner.services.EventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class NeoPaymentMonitor {
    @Autowired
    private EventPublisher eventPublisher;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @EventListener
    private void handleBlock(NewBlockEvent event) {
        if (event.getNetworkType().getNetworkProviderType() != NetworkProviderType.NEO) {
            return;
        }
        Set<String> addresses = event.getTransactionsByAddress().keySet();
        if (addresses.isEmpty()) {
            return;
        }

        subscriptionRepository.findSubscribedByAddressesListAndNetwork(addresses, event.getNetworkType())
                .forEach(subscription -> {
                    List<WrapperTransaction> transactions = event
                            .getTransactionsByAddress()
                            .get(subscription.getAddress());
                    if (transactions == null) {
                        log.warn("There is no Subscription entity found for NEO address {}.",
                                subscription.getAddress());
                        return;
                    }

                    for (WrapperTransaction tx : transactions) {
                        BigInteger inputValue = BigInteger.ZERO;
                        List<WrapperInputNeo> filteredInputs = tx
                                .getInputs()
                                .stream()
                                .map(input -> (WrapperInputNeo) input)
                                .filter(input -> Objects.equals(input.getAsset().name(), subscription.getCurrency()))
                                .collect(Collectors.toList());
                        for (WrapperInputNeo input : filteredInputs) {
                            if (input.getParentTransaction() == null) {
                                log.warn("Skip it. Input {} has not parent transaction.", input);
                                continue;
                            }
                            if (!input.getAddress().equalsIgnoreCase(subscription.getAddress())) {
                                continue;
                            }

                            inputValue = inputValue.add(input.getValue());
                        }

                        BigInteger outputValue = BigInteger.ZERO;
                        List<WrapperOutputNeo> filteredOutputs = tx
                                .getOutputs()
                                .stream()
                                .map(output -> (WrapperOutputNeo) output)
                                .filter(input -> Objects.equals(input.getAsset().name(), subscription.getCurrency()))
                                .collect(Collectors.toList());
                        for (WrapperOutputNeo output : filteredOutputs) {
                            if (output.getParentTransaction() == null) {
                                log.warn("Skip it. Output {} has not parent transaction.", output);
                                continue;
                            }
                            if (!output.getAddress().equalsIgnoreCase(subscription.getAddress())) {
                                continue;
                            }

                            outputValue = outputValue.add(output.getValue());
                        }

                        // write-off
                        if (!inputValue.equals(BigInteger.ZERO)) {
                            BigInteger amount = inputValue.subtract(outputValue).subtract(tx.getFee());

                            eventPublisher.publish(new PaymentEvent(
                                    subscription,
                                    event.getNetworkType(),
                                    event.getBlock(),
                                    tx,
                                    subscription.getAddress(),
                                    null,
                                    amount,
                                    tx.getFee(),
                                    null,
                                    subscription.getCurrency(),
                                    null,
                                    true
                            ));
                        }

                        // replenishment
                        if (!outputValue.equals(BigInteger.ZERO) && inputValue.equals(BigInteger.ZERO)) {
                            eventPublisher.publish(new PaymentEvent(
                                    subscription,
                                    event.getNetworkType(),
                                    event.getBlock(),
                                    tx,
                                    null,
                                    subscription.getAddress(),
                                    outputValue,
                                    tx.getFee(),
                                    null,
                                    subscription.getCurrency(),
                                    null,
                                    true
                            ));
                        }
                    }
                });
    }
}
