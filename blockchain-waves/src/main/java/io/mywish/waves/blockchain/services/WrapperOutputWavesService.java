package io.mywish.waves.blockchain.services;

import com.wavesplatform.wavesj.Transfer;
import com.wavesplatform.wavesj.transactions.MassTransferTransaction;
import com.wavesplatform.wavesj.transactions.TransferTransaction;
import io.mywish.blockchain.WrapperOutput;
import io.mywish.waves.blockchain.model.WrapperOutputWaves;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

@Component
public class WrapperOutputWavesService {
    public WrapperOutput build(TransferTransaction tx) {
        String hash = tx.getId().getBase58String();
        String sender = tx.getSenderPublicKey().getAddress();
        String recipient = tx.getRecipient();
        BigInteger amount = BigInteger.valueOf(tx.getAmount());
        String assetId = tx.getAssetId();
        return new WrapperOutputWaves(hash, sender, recipient, amount, assetId);
    }

    public WrapperOutput build(MassTransferTransaction tx, Transfer transfer) {
        String hash = tx.getId().getBase58String();
        String from = tx.getSenderPublicKey().getAddress();
        String to = transfer.getRecipient();
        BigInteger amount = BigInteger.valueOf(transfer.getAmount());
        String assetId = tx.getAssetId();
        return new WrapperOutputWaves(hash, from, to, amount, assetId);
    }
}
