package io.lastwill.eventscan.services.monitors.payments;

import io.lastwill.eventscan.model.NetworkProviderType;
import io.lastwill.eventscan.model.Subscription;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

@Component
public class BtcTcrPaymentMonitor extends UtxoPaymentMonitor {
    @Override
    protected BigInteger subtractFee(BigInteger value, BigInteger fee, Subscription subscription) {
        return value.subtract(fee);
    }

    @Override
    protected NetworkProviderType getSupportedNetwork() {
        return NetworkProviderType.BTC;
    }
}
