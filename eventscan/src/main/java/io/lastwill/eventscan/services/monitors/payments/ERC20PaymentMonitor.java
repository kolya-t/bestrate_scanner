package io.lastwill.eventscan.services.monitors.payments;

import io.lastwill.eventscan.events.model.SubscriptionPaymentEvent;
import io.lastwill.eventscan.events.model.contract.erc20.TransferEvent;
import io.lastwill.eventscan.model.NetworkProviderType;
import io.lastwill.eventscan.model.NetworkType;
import io.lastwill.eventscan.model.Subscription;
import io.lastwill.eventscan.repositories.CurrencyRepository;
import io.lastwill.eventscan.repositories.SubscriptionRepository;
import io.lastwill.eventscan.services.TransactionProvider;
import io.mywish.blockchain.WrapperTransaction;
import io.mywish.scanner.model.NewBlockEvent;
import io.mywish.scanner.services.EventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class ERC20PaymentMonitor {
    @Autowired
    private CurrencyRepository currencyRepository;

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

        currencyRepository.findAllByNetwork(event.getNetworkType())
                .stream()
                .filter(currency -> addresses.contains(currency.getTokenAddress()))
                .forEach(currency -> {
                    List<WrapperTransaction> transactions = event.getTransactionsByAddress().get(currency.getTokenAddress());
                    handle(currency.getTokenAddress(), transactions, currency.getSymbol(), event.getNetworkType());
                });
    }

    private void handle(final String tokenAddress, final List<WrapperTransaction> transactions, final String currency, NetworkType networkType) {
        for (final WrapperTransaction transaction : transactions) {
            if (!tokenAddress.equalsIgnoreCase(transaction.getOutputs().get(0).getAddress())) {
                continue;
            }

            transactionProvider.getTransactionReceiptAsync(networkType, transaction)
                    .thenAccept(transactionReceipt -> transactionReceipt.getLogs()
                            .stream()
                            .filter(event -> event instanceof TransferEvent)
                            .map(event -> (TransferEvent) event)
                            .forEach(eventValue -> {
                                List<Subscription> subscriptions = subscriptionRepository.findSubscribedByAddressesListAndNetwork(
                                        Arrays.asList(eventValue.getFrom(), eventValue.getTo()),
                                        networkType
                                );

                                subscriptions.forEach(subscription -> eventPublisher.publish(new SubscriptionPaymentEvent(
                                        networkType,
                                        transaction,
                                        subscription.getAddress().equals(eventValue.getTo())
                                                ? eventValue.getTokens()
                                                : eventValue.getTokens().negate(),
                                        tokenAddress,
                                        currency,
                                        subscription,
                                        true
                                )));
                            }))
                    .exceptionally(throwable -> {
                        log.error("Error on getting receipt for handling WISH payment.", throwable);
                        return null;
                    });
        }

    }
}
