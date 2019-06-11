package io.mywish.waves.blockchain.services;

import com.wavesplatform.wavesj.Transaction;
import com.wavesplatform.wavesj.transactions.MassTransferTransaction;
import com.wavesplatform.wavesj.transactions.TransferTransaction;
import io.mywish.blockchain.WrapperInput;
import io.mywish.blockchain.WrapperOutput;
import io.mywish.blockchain.service.WrapperTransactionService;
import io.mywish.waves.blockchain.model.WrapperTransactionWaves;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class WrapperTransactionWavesService implements WrapperTransactionService<Transaction> {
    @Autowired
    private WrapperOutputWavesService outputBuilder;

    @Override
    public WrapperTransactionWaves build(Transaction transaction) {
        String hash = transaction.getId().getBase58String();
        BigInteger fee = BigInteger.valueOf(transaction.getFee());

        List<WrapperInput> inputs = Collections.emptyList();
        List<WrapperOutput> outputs;

        if (transaction instanceof TransferTransaction) {
            TransferTransaction transferTx = (TransferTransaction) transaction;
            outputs = Collections.singletonList(outputBuilder.build(transferTx));
        } else if (transaction instanceof MassTransferTransaction) {
            MassTransferTransaction massTransferTx = (MassTransferTransaction) transaction;
            outputs = massTransferTx
                    .getTransfers()
                    .stream()
                    .map(transfer -> outputBuilder.build(massTransferTx, transfer))
                    .collect(Collectors.toList());
        } else {
            outputs = Collections.emptyList();
        }

        return new WrapperTransactionWaves(hash, inputs, outputs, fee);
    }
}
