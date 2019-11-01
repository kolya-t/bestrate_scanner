package io.lastwill.eventscan.services.monitors.payments;

import io.lastwill.eventscan.events.model.PaymentEvent;
import io.lastwill.eventscan.model.NetworkProviderType;
import io.lastwill.eventscan.model.NetworkType;
import io.lastwill.eventscan.model.Subscription;
import io.lastwill.eventscan.repositories.SubscriptionRepository;
import io.mywish.blockchain.WrapperInput;
import io.mywish.blockchain.WrapperOutput;
import io.mywish.blockchain.WrapperTransaction;
import io.mywish.scanner.model.NewBlockEvent;
import io.mywish.scanner.services.EventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public abstract class UtxoPaymentMonitor {
    @Autowired
    private EventPublisher eventPublisher;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @EventListener
    private void handleBlock(NewBlockEvent event) {
        if (!isSupportedNetwork(event.getNetworkType())) {
            return;
        }
        Set<String> addresses = event
                .getTransactionsByAddress()
                .keySet()
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        if (addresses.isEmpty()) {
            return;
        }
        subscriptionRepository.findSubscribedByAddressesListAndNetwork(addresses, event.getNetworkType())
                .forEach(subscription -> {
                    List<WrapperTransaction> transactions = event
                            .getTransactionsByAddress()
                            .get(subscription.getAddress());
                    if (transactions == null) {
                        log.warn("There is no Subscription entity found for TCR address {}.",
                                subscription.getAddress());
                        return;
                    }

                    for (WrapperTransaction tx : transactions) {
                        BigInteger inputValue = BigInteger.ZERO;
                        for (WrapperInput input : filterInputs(tx.getInputs(), subscription)) {
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
                        for (WrapperOutput output : filterOutputs(tx.getOutputs(), subscription)) {
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
                            BigInteger amount = subtractFee(inputValue.subtract(outputValue), tx.getFee(), subscription);

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

    protected abstract BigInteger subtractFee(BigInteger value, BigInteger fee, Subscription subscription);

    protected Collection<WrapperInput> filterInputs(Collection<WrapperInput> inputs, Subscription subscription) {
        return inputs;
    }

    protected Collection<WrapperOutput> filterOutputs(Collection<WrapperOutput> outputs, Subscription subscription) {
        return outputs;
    }

    protected abstract NetworkProviderType getSupportedNetwork();

    private boolean isSupportedNetwork(NetworkType networkType) {
        return getSupportedNetwork().equals(networkType.getNetworkProviderType());
    }
}
