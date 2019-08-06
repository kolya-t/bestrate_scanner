package io.mywish.waves.blockchain.services;

import io.mywish.blockchain.WrapperBlock;
import io.mywish.blockchain.WrapperTransaction;
import io.mywish.scanner.model.NewBlockEvent;
import io.mywish.scanner.services.LastBlockPersister;
import io.mywish.scanner.services.ScannerPolling;
import io.mywish.waves.blockchain.model.WrapperOutputWaves;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

@Slf4j
public class WavesScanner extends ScannerPolling {
    public WavesScanner(WavesNetwork network, LastBlockPersister lastBlockPersister, Long pollingInterval, Integer commitmentChainLength, Long reachInterval) {
        super(network, lastBlockPersister, pollingInterval, commitmentChainLength, reachInterval);
    }

    public WavesScanner(WavesNetwork network, LastBlockPersister lastBlockPersister, Long pollingInterval, Integer commitmentChainLength) {
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
        block.getTransactions().forEach(tx -> {
            tx.getOutputs()
                    .stream()
                    .map(output -> (WrapperOutputWaves) output)
                    .map(output -> Arrays.asList(output.getFrom(), output.getAddress()))
                    .flatMap(Collection::stream)
                    .filter(address -> !contains(addressTransactions, address, tx))
                    .forEach(address -> addressTransactions.add(address, tx));
        });
        eventPublisher.publish(new NewBlockEvent(network.getType(), block, addressTransactions));
    }

    private static <K, V> boolean contains(MultiValueMap<K, V> map, K key, V value) {
        List<V> list = map.get(key);
        return list != null && list.contains(value);
    }
}
