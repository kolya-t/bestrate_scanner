package io.lastwill.eventscan.services.monitors.payments;

import io.lastwill.eventscan.events.model.PaymentEvent;
import io.lastwill.eventscan.events.model.contract.eos.EosTransferEvent;
import io.lastwill.eventscan.model.NetworkProviderType;
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

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class EosPaymentMonitor {
    @Autowired
    private TransactionProvider transactionProvider;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private EventPublisher eventPublisher;

    @EventListener
    public void newBlockEvent(final NewBlockEvent newBlockEvent) {
        if (newBlockEvent.getNetworkType().getNetworkProviderType() != NetworkProviderType.EOS) {
            return;
        }

        Set<String> addresses = newBlockEvent.getTransactionsByAddress().keySet();
        if (addresses.isEmpty()) {
            return;
        }

        subscriptionRepository.findSubscribedByTokenAddress(addresses, newBlockEvent.getNetworkType())
                .forEach(subscription -> {
                    List<WrapperTransaction> transactions = newBlockEvent.getTransactionsByAddress().get(subscription.getTokenAddress());
                    if (transactions == null || transactions.isEmpty()) {
                        return;
                    }

                    transactions.forEach(transaction -> transaction.getOutputs().forEach(output -> {
                        if (!output.getAddress().equalsIgnoreCase(subscription.getTokenAddress())) {
                            return;
                        }

                        final WrapperTransactionReceipt receipt;
                        try {
                            receipt = transactionProvider.getTransactionReceipt(
                                    newBlockEvent.getNetworkType(),
                                    transaction
                            );
                        } catch (Exception e) {
                            log.error("Error on getting receipt tx {}.", transaction, e);
                            return;
                        }

                        receipt.getLogs()
                                .stream()
                                .filter(event -> event instanceof EosTransferEvent)
                                .map(event -> (EosTransferEvent) event)
                                .filter(event -> subscription.getTokenAddress().equalsIgnoreCase(event.getAddress()))
                                .filter(event -> subscription.getCurrency().equalsIgnoreCase(event.getSymbol()))
                                .filter(event -> subscription.getAddress().equalsIgnoreCase(event.getFrom())
                                        || subscription.getAddress().equalsIgnoreCase(event.getTo()))
                                .forEach(transferEvent -> {

                                    String memo = new String(transferEvent.getData(), StandardCharsets.US_ASCII);
                                    eventPublisher.publish(new PaymentEvent(
                                            subscription,
                                            newBlockEvent.getNetworkType(),
                                            newBlockEvent.getBlock(),
                                            transaction,
                                            transferEvent.getFrom(),
                                            transferEvent.getTo(),
                                            transferEvent.getTokens(),
                                            null,
                                            subscription.getTokenAddress(),
                                            subscription.getCurrency(),
                                            memo,
                                            receipt.isSuccess()
                                    ));
                                });
                    }));
                });
    }
}
