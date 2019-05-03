package io.mywish.web3.blockchain;

import io.lastwill.eventscan.model.NetworkType;
import io.lastwill.eventscan.repositories.LastBlockRepository;
import io.mywish.scanner.services.LastBlockDbPersister;
import io.mywish.scanner.services.LastBlockPersister;
import io.mywish.web3.blockchain.parity.Web3jEx;
import io.mywish.web3.blockchain.service.Web3Network;
import io.mywish.web3.blockchain.service.Web3Scanner;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.http.HttpService;

@Configuration
@ComponentScan
public class Web3BCModule {
    @ConditionalOnProperty(name = "io.lastwill.eventscan.web3-url.ethereum")
    @Bean(name = NetworkType.ETHEREUM_MAINNET_VALUE)
    public Web3Network ethNetMain(
            OkHttpClient client,
            @Value("${etherscanner.web3-url.ethereum}") String web3Url,
            @Value("${etherscanner.pending-transactions-threshold}") int pendingThreshold) {
        return new Web3Network(
                NetworkType.ETHEREUM_MAINNET,
                Web3jEx.build(new HttpService(web3Url, client, false)),
                pendingThreshold);
    }

    @ConditionalOnProperty(name = "io.lastwill.eventscan.web3-url.ropsten")
    @Bean(name = NetworkType.ETHEREUM_ROPSTEN_VALUE)
    public Web3Network ethNetRopsten(
            OkHttpClient client,
            @Value("${etherscanner.web3-url.ropsten}") String web3Url,
            @Value("${etherscanner.pending-transactions-threshold}") int pendingThreshold) {
        return new Web3Network(
                NetworkType.ETHEREUM_ROPSTEN,
                Web3jEx.build(new HttpService(web3Url, client, false)),
                pendingThreshold);
    }

    @Bean
    public LastBlockPersister ethMainnetLastBlockPersister(
            LastBlockRepository lastBlockRepository
    ) {
        return new LastBlockDbPersister(NetworkType.ETHEREUM_MAINNET, lastBlockRepository, null);
    }

    @Bean
    public LastBlockPersister ethRopstenLastBlockPersister(
            LastBlockRepository lastBlockRepository
    ) {
        return new LastBlockDbPersister(NetworkType.ETHEREUM_ROPSTEN, lastBlockRepository, null);
    }

    @ConditionalOnBean(name = NetworkType.ETHEREUM_MAINNET_VALUE)
    @Bean
    public Web3Scanner ethScannerMain(
            final @Qualifier(NetworkType.ETHEREUM_MAINNET_VALUE) Web3Network network,
            final @Qualifier("ethMainnetLastBlockPersister") LastBlockPersister lastBlockPersister,
            final @Value("${etherscanner.polling-interval-ms:5000}") Long pollingInterval,
            final @Value("${etherscanner.commit-chain-length:5}") Integer commitmentChainLength
    ) {
        return new Web3Scanner(
                network,
                lastBlockPersister,
                pollingInterval,
                commitmentChainLength
        );
    }

    @ConditionalOnBean(name = NetworkType.ETHEREUM_ROPSTEN_VALUE)
    @Bean
    public Web3Scanner ethScannerRopsten(
            final @Qualifier(NetworkType.ETHEREUM_ROPSTEN_VALUE) Web3Network network,
            final @Qualifier("ethRopstenLastBlockPersister") LastBlockPersister lastBlockPersister,
            final @Value("${etherscanner.polling-interval-ms:5000}") Long pollingInterval,
            final @Value("${etherscanner.commit-chain-length:5}") Integer commitmentChainLength
    ) {
        return new Web3Scanner(
                network,
                lastBlockPersister,
                pollingInterval,
                commitmentChainLength
        );
    }
}
