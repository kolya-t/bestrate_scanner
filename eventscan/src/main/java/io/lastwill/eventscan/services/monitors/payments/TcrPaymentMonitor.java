package io.lastwill.eventscan.services.monitors.payments;

import io.lastwill.eventscan.events.model.SubscriptionPaymentEvent;
import io.lastwill.eventscan.model.CryptoCurrency;
import io.lastwill.eventscan.model.NetworkType;
import io.lastwill.eventscan.repositories.SubscriptionRepository;
import io.mywish.blockchain.WrapperOutput;
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
public class TcrPaymentMonitor {
    @Autowired
    private EventPublisher eventPublisher;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @EventListener
    private void handleBtcBlock(NewBlockEvent event) {
        if (event.getNetworkType() != NetworkType.TECRA_MAINNET) {
            return;
        }
        Set<String> addresses = event.getTransactionsByAddress().keySet();
        if (addresses.isEmpty()) {
            return;
        }
        subscriptionRepository.findSubscribedByTcrAddressesList(addresses)
                .forEach(subscription -> {
                    List<WrapperTransaction> transactions = event
                            .getTransactionsByAddress()
                            .get(subscription.getTcrAddress());
                    if (transactions == null) {
                        log.warn("There is no Subscription entity found for TCR address {}.",
                                subscription.getTcrAddress());
                        return;
                    }

                    for (WrapperTransaction tx: transactions) {
                        for (WrapperOutput output: tx.getOutputs()) {
                            if (output.getParentTransaction() == null) {
                                log.warn("Skip it. Output {} has not parent transaction.", output);
                                continue;
                            }
                            if (!output.getAddress().equalsIgnoreCase(subscription.getTcrAddress())) {
                                continue;
                            }

                            eventPublisher.publish(new SubscriptionPaymentEvent(
                                    event.getNetworkType(),
                                    tx,
                                    output.getValue(),
                                    CryptoCurrency.TCR,
                                    subscription,
                                    true
                            ));
                        }
                    }
                });
    }
}
