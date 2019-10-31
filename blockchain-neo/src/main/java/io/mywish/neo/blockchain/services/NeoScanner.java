package io.mywish.neo.blockchain.services;

import io.mywish.blockchain.WrapperBlock;
import io.mywish.blockchain.WrapperInput;
import io.mywish.blockchain.WrapperOutput;
import io.mywish.blockchain.WrapperTransaction;
import io.mywish.scanner.model.NewBlockEvent;
import io.mywish.scanner.services.LastBlockPersister;
import io.mywish.scanner.services.ScannerPolling;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class NeoScanner extends ScannerPolling {
    public NeoScanner(NeoNetwork network, LastBlockPersister lastBlockPersister, Long pollingInterval, Integer commitmentChainLength) {
        super(network, lastBlockPersister, pollingInterval, commitmentChainLength);
    }

    @Override
    protected void processBlock(WrapperBlock block) {
        log.info("{}: new block received {} ({})", network.getType(), block.getNumber(), block.getHash());

        MultiValueMap<String, WrapperTransaction> addressTransactions = CollectionUtils.toMultiValueMap(new HashMap<>());

        if (block.getTransactions() == null) {
            log.warn("{}: block {} has no transactions.", network.getType(), block.getNumber());
            return;
        }

        block.getTransactions()
                .forEach(transaction -> Stream.concat(
                        transaction
                                .getInputs()
                                .stream()
                                .map(WrapperInput::getAddress),
                        transaction
                                .getOutputs()
                                .stream()
                                .map(WrapperOutput::getAddress)
                        )
                                .filter(address -> !contains(addressTransactions, address, transaction))
                                .forEach(address -> addressTransactions.add(address, transaction))
                );
        eventPublisher.publish(new NewBlockEvent(network.getType(), block, addressTransactions));
    }

    private static <K, V> boolean contains(MultiValueMap<K, V> map, K key, V value) {
        List<V> list = map.get(key);
        return list != null && list.contains(value);
    }
}
