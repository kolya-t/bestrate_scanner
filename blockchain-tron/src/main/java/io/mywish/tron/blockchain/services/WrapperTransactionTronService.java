package io.mywish.tron.blockchain.services;

import com.fasterxml.jackson.databind.JsonNode;
import io.mywish.blockchain.WrapperInput;
import io.mywish.blockchain.WrapperOutput;
import io.mywish.blockchain.WrapperTransaction;
import io.mywish.blockchain.service.WrapperTransactionService;
import io.mywish.tron.blockchain.model.WrapperTransactionTron;
import io.mywish.troncli4j.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

@Component
public class WrapperTransactionTronService implements WrapperTransactionService<Transaction> {
    private static final String TRANSFER_TYPE = "TransferContract";
    @Autowired
    private WrapperOutputTronService outputBuilder;

    @Override
    public WrapperTransaction build(Transaction transaction) {

        String hash = transaction.getTxId();
        Transaction.Contract contractWrapper = transaction.getRawData().getContract().get(0);
        String contractType = contractWrapper.getType();
        if (!contractType.equals(TRANSFER_TYPE)) {
            return null;
        }
        JsonNode contract = contractWrapper.getParameter().getValue();

        String ownerAddress = contract.get("owner_address").asText();
        BigInteger amount = BigInteger.valueOf(contract.get("amount").asLong());

        List<WrapperInput> inputs = Collections.singletonList(new WrapperInput(transaction.getTxId(), ownerAddress, amount));

        List<WrapperOutput> outputs = Collections.singletonList(outputBuilder.build(transaction));


        List<String> contracts = transaction.getContractAddress() == null
                ? Collections.emptyList()
                : Collections.singletonList(transaction.getContractAddress());

        WrapperTransactionTron res = new WrapperTransactionTron(
                hash,
                inputs,
                outputs,
                false,
                contracts,
                transaction.getStatus(),
                transaction.getFee()
        );

        return res;
    }
}
