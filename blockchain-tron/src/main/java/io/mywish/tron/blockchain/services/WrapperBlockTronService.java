package io.mywish.tron.blockchain.services;

import io.mywish.blockchain.WrapperBlock;
import io.mywish.blockchain.WrapperTransaction;
import io.mywish.blockchain.service.WrapperBlockService;
import io.mywish.troncli4j.model.response.BlockResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class WrapperBlockTronService implements WrapperBlockService<BlockResponse> {
    @Autowired
    WrapperTransactionTronService transactionBuilder;

    @Override
    public WrapperBlock build(BlockResponse block) {
        String hash = block.getBlockId();
        BlockResponse.RawData header = block.getBlockHeader();
        Long number = header.getNumber();
        Instant timestamp = Instant.ofEpochMilli(header.getTimestamp());
        List<WrapperTransaction> transactions = block
                .getTransactions()
                .stream()
                .map(transactionBuilder::build)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return new WrapperBlock(hash, number, timestamp, transactions);
    }
}
