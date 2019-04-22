package io.mywish.tecra.blockchain.services;

import com.neemre.btcdcli4j.core.client.BtcdClient;
import io.lastwill.eventscan.model.NetworkType;
import io.mywish.blockchain.WrapperBlock;
import io.mywish.blockchain.WrapperNetwork;
import io.mywish.blockchain.WrapperTransaction;
import io.mywish.tecra.blockchain.helper.TecraBlockParser;
import org.bitcoinj.core.NetworkParameters;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigInteger;
import java.util.List;

public class TecraNetwork extends WrapperNetwork {
    private final BtcdClient btcdClient;
    private final NetworkParameters networkParameters;

    @Autowired
    private WrapperBlockTecraService blockBuilder;

    @Autowired
    private TecraBlockParser tecraBlockParser;

    public TecraNetwork(NetworkType type, BtcdClient btcdClient, NetworkParameters networkParameters) {
        super(type);
        this.btcdClient = btcdClient;
        this.networkParameters = networkParameters;
    }

    @Override
    public Long getLastBlock() throws Exception {
        return btcdClient.getBlockCount().longValue();
    }

    @Override
    public WrapperBlock getBlock(String hash) throws Exception {
        // TODO optimize
        long height = btcdClient.getBlock(hash).getHeight();
        return blockBuilder.build(
                tecraBlockParser.parse(
                        networkParameters,
                        (String) btcdClient.getBlock(hash, false)
                ),
                height,
                networkParameters
        );
    }

    @Override
    public WrapperBlock getBlock(Long number) throws Exception {
        String hash = btcdClient.getBlockHash(number.intValue());
        return getBlock(hash);
    }

    @Override
    public BigInteger getBalance(String address, Long blockNo) {
        throw new UnsupportedOperationException("Method not supported");
    }

    @Override
    public boolean isPendingTransactionsSupported() {
        return false;
    }

    @Override
    public List<WrapperTransaction> fetchPendingTransactions() {
        throw new UnsupportedOperationException("Method not supported");
    }
}
