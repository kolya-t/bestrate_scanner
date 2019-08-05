package io.lastwill.eventscan.services.monitors.payments;

import io.lastwill.eventscan.events.model.PaymentEvent;
import io.lastwill.eventscan.model.NetworkProviderType;
import io.lastwill.eventscan.model.Subscription;
import io.lastwill.eventscan.repositories.SubscriptionRepository;
import io.lastwill.eventscan.services.TransactionProvider;
import io.mywish.blockchain.WrapperTransaction;
import io.mywish.blockchain.WrapperTransactionReceipt;
import io.mywish.scanner.model.NewBlockEvent;
import io.mywish.scanner.services.EventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class EthPaymentMonitor {
    @Autowired
    private EventPublisher eventPublisher;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private TransactionProvider transactionProvider;

    @EventListener
    private void onNewBlockEvent(NewBlockEvent event) {
        if (event.getNetworkType().getNetworkProviderType() != NetworkProviderType.WEB3) {
            return;
        }

        Set<String> addresses = event.getTransactionsByAddress().keySet();
        if (addresses.isEmpty()) {
            return;
        }

        List<Subscription> subscriptions = subscriptionRepository.findSubscribedByAddressesListAndNetwork(addresses, event.getNetworkType());
        if (subscriptions.isEmpty()) {
            return;
        }

        Map<String, List<Subscription>> subscriptionAddresses = subscriptions
                .stream()
                .collect(Collectors.groupingBy(
                        subscription -> subscription.getAddress().toLowerCase(),
                        Collectors.mapping(Function.identity(), Collectors.toList())
                ));

        event.getBlock()
                .getTransactions()
                .stream()
                // skip contract creations
                .filter(transaction -> transaction.getSingleOutputAddress() != null)
                .filter(transaction -> subscriptionAddresses.containsKey(transaction.getSingleInputAddress().toLowerCase())
                        || subscriptionAddresses.containsKey(transaction.getSingleOutputAddress().toLowerCase()))
                .forEach(transaction -> {
                    WrapperTransactionReceipt receipt;
                    try {
                        receipt = transactionProvider.getTransactionReceipt(event.getNetworkType(), transaction);
                    }
                    catch (Exception e) {
                        log.error("Failed to get transaction receipt.", e);
                        return;
                    }
                    Stream.concat(
                            subscriptionAddresses.getOrDefault(transaction.getSingleInputAddress().toLowerCase(), Collections.emptyList()).stream(),
                            subscriptionAddresses.getOrDefault(transaction.getSingleOutputAddress().toLowerCase(), Collections.emptyList()).stream()
                    )
                            .forEach(subscription -> {
                                BigInteger amount = getAmountFor(transaction.getSingleOutputAddress(), transaction);
                                if (amount.equals(BigInteger.ZERO)) {
                                    return;
                                }

                                eventPublisher.publish(new PaymentEvent(
                                        subscription,
                                        event.getNetworkType(),
                                        event.getBlock(),
                                        transaction,
                                        transaction.getSingleInputAddress(),
                                        transaction.getSingleOutputAddress(),
                                        amount,
                                        null,
                                        null,
                                        "ETH",
                                        null,
                                        receipt.isSuccess()
                                ));
                            });
                });
    }

    private BigInteger getAmountFor(final String address, final WrapperTransaction transaction) {
        BigInteger result = BigInteger.ZERO;
        if (address.equalsIgnoreCase(transaction.getInputs().get(0).getAddress())) {
            result = result.subtract(transaction.getOutputs().get(0).getValue());
        }
        if (address.equalsIgnoreCase(transaction.getOutputs().get(0).getAddress())) {
            result = result.add(transaction.getOutputs().get(0).getValue());
        }
        return result;
    }

}
