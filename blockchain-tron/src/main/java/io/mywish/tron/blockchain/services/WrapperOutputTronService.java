package io.mywish.tron.blockchain.services;

import com.fasterxml.jackson.databind.JsonNode;
import io.mywish.blockchain.WrapperOutput;
import io.mywish.blockchain.service.WrapperOutputService;
import io.mywish.tron.blockchain.model.WrapperOutputTron;
import io.mywish.troncli4j.model.Transaction;
import io.mywish.troncli4j.model.contracttype.ContractType;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

@Component
public class WrapperOutputTronService implements WrapperOutputService<Transaction> {
    @Override
    public WrapperOutput build(Transaction transaction) {
        Transaction.Contract contractWrapper = transaction.getRawData().getContract().get(0);
        JsonNode contract = contractWrapper.getParameter().getValue();

        String address = contract.get("to_address").asText();
        BigInteger value = contract.get("amount").bigIntegerValue();

        return new WrapperOutputTron(
                transaction.getTxId(),
                address,
                value,
                contract
        );
    }
}
