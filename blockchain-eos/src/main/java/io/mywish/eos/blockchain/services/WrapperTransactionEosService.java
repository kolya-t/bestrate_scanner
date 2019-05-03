package io.mywish.eos.blockchain.services;

import io.mywish.blockchain.WrapperInput;
import io.mywish.blockchain.WrapperOutput;
import io.mywish.blockchain.WrapperTransaction;
import io.mywish.blockchain.service.WrapperTransactionService;
import io.mywish.eos.blockchain.model.WrapperOutputEos;
import io.mywish.eos.blockchain.model.WrapperTransactionEos;
import io.mywish.eoscli4j.model.ActionAuthorization;
import io.mywish.eoscli4j.model.EosAction;
import io.mywish.eoscli4j.model.Transaction;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class WrapperTransactionEosService implements WrapperTransactionService<Transaction> {
    private final static String ACCOUNT_NAME_SEPARATOR = "::";

    @Override
    public WrapperTransaction build(Transaction transaction) {
        String hash = transaction.getId();
        List<WrapperInput> inputs = transaction
                .getActions()
                .stream()
                .map(EosAction::getAuthorizations)
                .flatMap(List::stream)
                .map(ActionAuthorization::getActor)
                .map(actor -> new WrapperInput(transaction.getId(), actor, BigInteger.ZERO))
                .collect(Collectors.toList());

        List<WrapperOutput> outputs = transaction
                .getActions()
                .stream()
                .map(eosAction -> new WrapperOutputEos(
                        transaction.getId(),
                        eosAction.getAccount(),
                        eosAction.getName(),
                        eosAction.getData()
                ))
                .collect(Collectors.toList());

        Boolean contractCreation = false;
        return new WrapperTransactionEos(
                hash,
                inputs,
                outputs,
                contractCreation,
                transaction.getStatus()
        );
    }
}
