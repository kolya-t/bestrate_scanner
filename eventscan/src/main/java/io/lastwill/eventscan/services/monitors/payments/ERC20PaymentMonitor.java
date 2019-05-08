package io.lastwill.eventscan.services.monitors.payments;

import io.lastwill.eventscan.events.model.PaymentEvent;
import io.lastwill.eventscan.events.model.contract.erc20.TransferEvent;
import io.lastwill.eventscan.model.NetworkProviderType;
import io.lastwill.eventscan.model.NetworkType;
import io.lastwill.eventscan.model.Subscription;
import io.lastwill.eventscan.repositories.SubscriptionRepository;
import io.lastwill.eventscan.services.TransactionProvider;
import io.mywish.blockchain.WrapperBlock;
import io.mywish.blockchain.WrapperTransaction;
import io.mywish.scanner.model.NewBlockEvent;
import io.mywish.scanner.services.EventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class ERC20PaymentMonitor {
    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private EventPublisher eventPublisher;

    @Autowired
    private TransactionProvider transactionProvider;

    @EventListener
    public void onNewBlock(final NewBlockEvent event) {
        if (event.getNetworkType().getNetworkProviderType() != NetworkProviderType.WEB3) {
            return;
        }
        Set<String> addresses = event.getTransactionsByAddress().keySet();
        if (addresses.isEmpty()) {
            return;
        }

        subscriptionRepository
                .findSubscribedByTokenAddress(addresses, event.getNetworkType())
                .forEach(subscription -> {
                    List<WrapperTransaction> transactions = event.getTransactionsByAddress().get(subscription.getTokenAddress());
                    handle(event.getBlock(), transactions, subscription, event.getNetworkType());
                });
    }

    private void handle(WrapperBlock block, List<WrapperTransaction> transactions, Subscription subscription, NetworkType networkType) {
        for (final WrapperTransaction transaction : transactions) {
            if (!subscription.getTokenAddress().equalsIgnoreCase(transaction.getOutputs().get(0).getAddress())) {
                continue;
            }

            transactionProvider.getTransactionReceiptAsync(networkType, transaction)
                    .thenAccept(transactionReceipt -> transactionReceipt.getLogs()
                            .stream()
                            .filter(event -> event instanceof TransferEvent)
                            .map(event -> (TransferEvent) event)
                            .forEach(eventValue -> {
                                eventPublisher.publish(new PaymentEvent(
                                        subscription,
                                        networkType,
                                        block,
                                        transaction,
                                        eventValue.getFrom(),
                                        eventValue.getTo(),
                                        eventValue.getTokens(),
                                        null,
                                        subscription.getTokenAddress(),
                                        subscription.getCurrency(),
                                        null,
                                        transactionReceipt.isSuccess()
                                ));
                            }))
                    .exceptionally(throwable -> {
                        log.error("Error on getting receipt for handling WISH payment.", throwable);
                        return null;
                    });
        }
    }
}
