package io.lastwill.eventscan.services.monitors.payments;

import io.lastwill.eventscan.model.NetworkProviderType;
import io.lastwill.eventscan.model.Subscription;
import io.mywish.blockchain.WrapperInput;
import io.mywish.blockchain.WrapperOutput;
import io.mywish.neo.blockchain.model.Asset;
import io.mywish.neo.blockchain.model.WrapperInputNeo;
import io.mywish.neo.blockchain.model.WrapperOutputNeo;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class NeoPaymentMonitor extends UtxoPaymentMonitor {
    @Override
    protected BigInteger subtractFee(BigInteger value, BigInteger fee, Subscription subscription) {
        if (Objects.equals(Asset.GAS.toString(), subscription.getCurrency())) {
            return value.subtract(fee);
        }
        return value;
    }

    @Override
    protected Collection<WrapperInput> filterInputs(Collection<WrapperInput> inputs, Subscription subscription) {
        return inputs
                .stream()
                .map(input -> (WrapperInputNeo) input)
                .filter(input -> Objects.equals(input.getAsset().toString(), subscription.getCurrency()))
                .collect(Collectors.toList());
    }

    @Override
    protected Collection<WrapperOutput> filterOutputs(Collection<WrapperOutput> outputs, Subscription subscription) {
        return outputs
                .stream()
                .map(output -> (WrapperOutputNeo) output)
                .filter(input -> Objects.equals(input.getAsset().toString(), subscription.getCurrency()))
                .collect(Collectors.toList());
    }

    @Override
    protected NetworkProviderType getSupportedNetwork() {
        return NetworkProviderType.NEO;
    }
}
