package io.lastwill.eventscan.services.monitors.payments;

import io.lastwill.eventscan.events.model.PaymentEvent;
import io.lastwill.eventscan.model.NetworkProviderType;
import io.lastwill.eventscan.model.NetworkType;
import io.lastwill.eventscan.model.Subscription;
import io.lastwill.eventscan.repositories.SubscriptionRepository;
import io.lastwill.eventscan.services.AddressConverter;
import io.mywish.blockchain.WrapperOutput;
import io.mywish.blockchain.WrapperTransaction;
import io.mywish.scanner.model.NewBlockEvent;
import io.mywish.scanner.services.EventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TronPaymentMonitor {
    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private EventPublisher eventPublisher;

    @EventListener
    public void newBlockEvent(final NewBlockEvent newBlockEvent) {
        NetworkType networkType = newBlockEvent.getNetworkType();
        if (networkType.getNetworkProviderType() != NetworkProviderType.TRON) {
            return;
        }
        Set<String> addresses = newBlockEvent
                .getTransactionsByAddress()
                .keySet()
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        if (addresses.isEmpty()) {
            return;
        }

        for (Subscription subscription : subscriptionRepository.findSubscribedByNetwork(networkType)) {
            for (String address : addresses) {
                if (!isAddressesEquals(subscription.getAddress(), address)) {
                    continue;
                }

                for (WrapperTransaction transaction : newBlockEvent.getTransactionsByAddress().get(address)) {
                    for (WrapperOutput output : transaction.getOutputs()) {
                        String fromAddress = transaction.getInputs().get(0).getAddress();
                        String toAddress = output.getAddress();

                        if (isAddressesEquals(fromAddress, subscription.getAddress())) {
                            fromAddress = subscription.getAddress();
                            if (!isHexadecimalAddress(fromAddress)) {
                                toAddress = convertAddress(toAddress);
                            }
                        } else {
                            toAddress = subscription.getAddress();
                            if (!isHexadecimalAddress(toAddress)) {
                                fromAddress = convertAddress(fromAddress);
                            }
                        }

                        eventPublisher.publish(new PaymentEvent(
                                subscription,
                                networkType,
                                newBlockEvent.getBlock(),
                                transaction,
                                fromAddress,
                                toAddress,
                                output.getValue(),
                                null,
                                subscription.getTokenAddress(),
                                subscription.getCurrency(),
                                null,
                                true
                        ));
                    }
                }
            }
        }
    }

    private boolean isHexadecimalAddress(String address) {
        return address.substring(0, 2).equalsIgnoreCase("41");
    }

    private String convertAddress(String address) {
        String result = address;
        if (isHexadecimalAddress(address)) {
            result = AddressConverter.toTronPubKeyFrom(address);
        }
        return result;
    }

    private boolean isAddressesEquals(String first, String second) {
        return convertAddress(first).equals(convertAddress(second));
    }
}
