package io.lastwill.eventscan.services.monitors.payments;

import io.lastwill.eventscan.events.model.SubscriptionPaymentEvent;
import io.lastwill.eventscan.events.model.contract.eos.EosTransferEvent;
import io.lastwill.eventscan.model.NetworkProviderType;
import io.lastwill.eventscan.model.Subscription;
import io.lastwill.eventscan.repositories.CurrencyRepository;
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

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class EosPaymentMonitor {
    @Autowired
    private TransactionProvider transactionProvider;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private EventPublisher eventPublisher;

    @EventListener
    public void newBlockEvent(final NewBlockEvent newBlockEvent) {
        if (newBlockEvent.getNetworkType().getNetworkProviderType() != NetworkProviderType.EOS) {
            return;
        }

        currencyRepository
                .findAllByNetwork(newBlockEvent.getNetworkType())
                .forEach(currency -> {
                    List<WrapperTransaction> transactions = newBlockEvent.getTransactionsByAddress().get(currency.getTokenAddress());
                    if (transactions == null || transactions.isEmpty()) {
                        return;
                    }

                    transactions.forEach(transaction -> transaction.getOutputs().forEach(output -> {
                        if (!output.getAddress().equalsIgnoreCase(currency.getTokenAddress())) {
                            return;
                        }

                        final WrapperTransactionReceipt receipt;
                        try {
                            receipt = transactionProvider.getTransactionReceipt(
                                    newBlockEvent.getNetworkType(),
                                    transaction
                            );
                        }
                        catch (Exception e) {
                            log.error("Error on getting receipt tx {}.", transaction, e);
                            return;
                        }

                        receipt.getLogs()
                                .stream()
                                .filter(event -> event instanceof EosTransferEvent)
                                .map(event -> (EosTransferEvent) event)
                                .filter(event -> currency.getTokenAddress().equalsIgnoreCase(event.getAddress()))
                                .filter(event -> currency.getSymbol().equalsIgnoreCase(event.getSymbol()))
                                .forEach(transferEvent -> {
                                    List<Subscription> subscriptions = subscriptionRepository.findSubscribedByAddressesListAndNetwork(
                                                    Arrays.asList(transferEvent.getFrom(), transferEvent.getTo()),
                                                    newBlockEvent.getNetworkType());

                                    subscriptions.forEach(subscription -> eventPublisher.publish(new SubscriptionPaymentEvent(
                                            newBlockEvent.getNetworkType(),
                                            transaction,
                                            subscription.getAddress().equals(transferEvent.getTo())
                                                    ? transferEvent.getTokens()
                                                    : transferEvent.getTokens().negate(),
                                            currency.getTokenAddress(),
                                            currency.getSymbol(),
                                            subscription,
                                            true
                                    )));
                                });
                    }));
                });
    }
}
