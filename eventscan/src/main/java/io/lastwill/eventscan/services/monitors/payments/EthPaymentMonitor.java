package io.lastwill.eventscan.services.monitors.payments;

import io.lastwill.eventscan.events.model.PaymentEvent;
import io.lastwill.eventscan.model.NetworkProviderType;
import io.lastwill.eventscan.model.Subscription;
import io.lastwill.eventscan.repositories.SubscriptionRepository;
import io.lastwill.eventscan.services.TransactionProvider;
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
        for (Subscription subscription : subscriptions) {
            final List<WrapperTransaction> transactions = event.getTransactionsByAddress().get(
                    subscription.getAddress().toLowerCase()
            );

            if (transactions == null) {
                log.error("User {} received from DB, but was not found in transaction list (block {}).", subscription, event.getBlock().getNumber());
                continue;
            }

            transactions.forEach(transaction -> {
                if (Stream.of(transaction.getSingleInputAddress(), transaction.getSingleOutputAddress())
                        .anyMatch(address -> subscription.getAddress().equalsIgnoreCase(address))) {
                    transactionProvider.getTransactionReceiptAsync(event.getNetworkType(), transaction)
                            .thenAccept(receipt -> {
                                eventPublisher.publish(new PaymentEvent(
                                        subscription,
                                        event.getNetworkType(),
                                        event.getBlock(),
                                        transaction,
                                        transaction.getSingleInputAddress(),
                                        transaction.getSingleOutputAddress(),
                                        getAmountFor(transaction.getSingleOutputAddress(), transaction),
                                        null,
                                        null,
                                        "ETH",
                                        null,
                                        receipt.isSuccess()
                                ));
                            });
                }
            });
        }
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
