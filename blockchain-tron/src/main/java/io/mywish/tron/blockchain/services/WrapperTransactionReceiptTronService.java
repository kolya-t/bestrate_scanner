package io.mywish.tron.blockchain.services;

import io.mywish.blockchain.ContractEvent;
import io.mywish.blockchain.WrapperTransactionReceipt;
import io.mywish.tron.blockchain.model.WrapperTransactionTron;
import io.mywish.troncli4j.model.EventResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class WrapperTransactionReceiptTronService {
    private static final String TRANSACTION_STATUS = "SUCCESS";
    @Autowired
    private WrapperLogTronService logBuilder;

    public WrapperTransactionReceipt build(WrapperTransactionTron transaction, List<EventResult> events) {
        List<String> contracts = transaction.getContracts();

        List<ContractEvent> logs = events
                .stream()
                .map(event -> buildEvent(transaction, event))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        boolean success = transaction.getStatus().equals(TRANSACTION_STATUS);

        return new WrapperTransactionReceipt(
                transaction.getHash(),
                contracts,
                logs,
                success
        );
    }

    private ContractEvent buildEvent(WrapperTransactionTron transaction, EventResult event) {
        try {
            return logBuilder.build(transaction, event);
        } catch (Exception e) {
            log.warn("Impossible to build event from Tron event {}, contract {}.", event.getEventName(), event.getContractAddress(), e);
            return null;
        }
    }

}
