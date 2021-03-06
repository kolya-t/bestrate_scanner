package io.mywish.btc.blockchain.services;

import io.mywish.blockchain.WrapperInput;
import io.mywish.blockchain.WrapperOutput;
import io.mywish.blockchain.WrapperTransaction;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class WrapperTransactionBtcService {
    @Autowired
    private WrapperInputBtcService inputBuilder;
    @Autowired
    private WrapperOutputBtcService outputBuilder;

    public WrapperTransaction build(Transaction transaction, NetworkParameters networkParameters) {
        String hash = transaction.getHashAsString();
        List<WrapperInput> inputs = transaction.getInputs().stream()
                .map(input -> inputBuilder.build(transaction, input, networkParameters))
                .filter(Objects::nonNull)
                .filter(wrapperInput -> wrapperInput.getAddress() != null)
                .collect(Collectors.toList());
        List<WrapperOutput> outputs = transaction.getOutputs().stream()
                .map(output -> outputBuilder.build(transaction, output, networkParameters))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        BigInteger fee = null;
        if (transaction.getFee() != null) {
            fee = BigInteger.valueOf(transaction.getFee().getValue());
        }

        return new WrapperTransaction(
                hash,
                inputs,
                outputs,
                false,
                null,
                fee
        );
    }
}
