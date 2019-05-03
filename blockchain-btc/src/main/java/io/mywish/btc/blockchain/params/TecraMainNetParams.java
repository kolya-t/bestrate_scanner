package io.mywish.btc.blockchain.params;

import org.bitcoinj.params.MainNetParams;

public class TecraMainNetParams extends MainNetParams {
    public TecraMainNetParams() {
        super();
        dnsSeeds = new String[]{
                "seed.tecracoin.io",
                "seed2.tecracoin.io"
        };
        addressHeader = 65;
        p2shHeader = 27;
    }
}
