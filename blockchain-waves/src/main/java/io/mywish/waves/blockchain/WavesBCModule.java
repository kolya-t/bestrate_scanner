package io.mywish.waves.blockchain;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.lastwill.eventscan.model.NetworkType;
import io.lastwill.eventscan.repositories.LastBlockRepository;
import io.mywish.scanner.services.LastBlockDbPersister;
import io.mywish.scanner.services.LastBlockPersister;
import io.mywish.waves.blockchain.services.WavesNetwork;
import io.mywish.waves.blockchain.services.WavesScanner;
import io.mywish.wavescli4j.service.WavesClientImpl;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
public class WavesBCModule {
    @ConditionalOnProperty("etherscanner.waves.rpc-url.mainnet")
    @Bean(name = NetworkType.WAVES_MAINNET_VALUE)
    public WavesNetwork wavesNetMain(
            final CloseableHttpClient httpClient,
            final @Value("${etherscanner.waves.rpc-url.mainnet}") String rpcUrl,
            ObjectMapper objectMapper
    ) {
        return new WavesNetwork(
                NetworkType.WAVES_MAINNET,
                new WavesClientImpl(
                        httpClient,
                        rpcUrl,
                        objectMapper
                )
        );
    }

    @ConditionalOnProperty("etherscanner.waves.rpc-url.testnet")
    @Bean(name = NetworkType.WAVES_TESTNET_VALUE)
    public WavesNetwork wavesNetTest(
            final CloseableHttpClient httpClient,
            final @Value("${etherscanner.waves.rpc-url.testnet}") String rpcUrl,
            final ObjectMapper objectMapper
    ) {
        return new WavesNetwork(
                NetworkType.WAVES_TESTNET,
                new WavesClientImpl(
                        httpClient,
                        rpcUrl,
                        objectMapper
                )
        );
    }

    @ConditionalOnBean(name = NetworkType.WAVES_MAINNET_VALUE)
    @Bean
    public WavesScanner wavesScannerMain(
            final @Qualifier(NetworkType.WAVES_MAINNET_VALUE) WavesNetwork network,
            final @Qualifier("wavesMainnetLastBlockPersister") LastBlockPersister lastBlockPersister,
            final @Value("${etherscanner.waves.polling-interval-ms}") Long pollingInterval,
            final @Value("${etherscanner.waves.commit-chain-length}") Integer commitmentChainLength,
            final @Value("${etherscanner.waves.reach-interval-ms}") Long reachInterval
    ) {
        return new WavesScanner(
                network,
                lastBlockPersister,
                pollingInterval,
                commitmentChainLength,
                reachInterval
        );
    }

    @ConditionalOnBean(name = NetworkType.WAVES_TESTNET_VALUE)
    @Bean
    public WavesScanner wavesScannerTest(
            final @Qualifier(NetworkType.WAVES_TESTNET_VALUE) WavesNetwork network,
            final @Qualifier("wavesTestnetLastBlockPersister") LastBlockPersister lastBlockPersister,
            final @Value("${etherscanner.waves.polling-interval-ms}") Long pollingInterval,
            final @Value("${etherscanner.waves.commit-chain-length}") Integer commitmentChainLength
    ) {
        return new WavesScanner(
                network,
                lastBlockPersister,
                pollingInterval,
                commitmentChainLength
        );
    }

    @Configuration
    public static class DbPersisterConfiguration {
        @Bean
        public LastBlockPersister wavesMainnetLastBlockPersister(
                LastBlockRepository lastBlockRepository,
                final @Value("${etherscanner.waves.last-block.mainnet:#{null}}") Long lastBlock
        ) {
            return new LastBlockDbPersister(NetworkType.WAVES_MAINNET, lastBlockRepository, lastBlock);
        }

        @Bean
        public LastBlockPersister wavesTestnetLastBlockPersister(
                LastBlockRepository lastBlockRepository,
                final @Value("${etherscanner.waves.last-block.testnet:#{null}}") Long lastBlock
        ) {
            return new LastBlockDbPersister(NetworkType.WAVES_TESTNET, lastBlockRepository, lastBlock);
        }
    }
}
