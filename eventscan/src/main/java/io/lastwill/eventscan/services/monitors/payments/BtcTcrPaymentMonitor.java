package io.lastwill.eventscan.services.monitors.payments;

import io.lastwill.eventscan.events.model.SubscriptionPaymentEvent;
import io.lastwill.eventscan.model.NetworkProviderType;
import io.lastwill.eventscan.model.NetworkType;
import io.lastwill.eventscan.repositories.SubscriptionRepository;
import io.mywish.blockchain.WrapperInput;
import io.mywish.blockchain.WrapperOutput;
import io.mywish.blockchain.WrapperTransaction;
import io.mywish.scanner.model.NewBlockEvent;
import io.mywish.scanner.services.EventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class BtcTcrPaymentMonitor {
    @Autowired
    private EventPublisher eventPublisher;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    private Map<NetworkType, String> currencyByNetwork = new HashMap<>();

    @PostConstruct
    protected void init() {
        currencyByNetwork.put(NetworkType.BTC_MAINNET, "BTC");
        currencyByNetwork.put(NetworkType.BTC_TESTNET_3, "BTC");
        currencyByNetwork.put(NetworkType.TECRA_MAINNET, "TCR");
    }

    @EventListener
    private void handleTecraBlock(NewBlockEvent event) {
        if (event.getNetworkType().getNetworkProviderType() != NetworkProviderType.BTC) {
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
                        log.warn("There is no Subscription entity found for TCR address {}.",
                                subscription.getAddress());
                        return;
                    }

                    for (WrapperTransaction tx: transactions) {
                        for (WrapperOutput output: tx.getOutputs()) {
                            if (output.getParentTransaction() == null) {
                                log.warn("Skip it. Output {} has not parent transaction.", output);
                                continue;
                            }
                            if (!output.getAddress().equalsIgnoreCase(subscription.getAddress())) {
                                continue;
                            }

                            eventPublisher.publish(new SubscriptionPaymentEvent(
                                    event.getNetworkType(),
                                    tx,
                                    output.getValue(),
                                    currencyByNetwork.get(event.getNetworkType()),
                                    subscription,
                                    true
                            ));
                        }

                        BigInteger sentValue = BigInteger.ZERO;
                        for (WrapperInput input : tx.getInputs()) {
                            if (input.getParentTransaction() == null) {
                                log.warn("Skip it. Input {} has not parent transaction.", input);
                                continue;
                            }
                            if (!input.getAddress().equalsIgnoreCase(subscription.getAddress())) {
                                continue;
                            }

                            sentValue = sentValue.add(input.getValue());
                        }

                        if (!sentValue.equals(BigInteger.ZERO)) {
                            eventPublisher.publish(new SubscriptionPaymentEvent(
                                    event.getNetworkType(),
                                    tx,
                                    sentValue.negate(),
                                    currencyByNetwork.get(event.getNetworkType()),
                                    subscription,
                                    true
                            ));
                        }
                    }
                });
    }
}
