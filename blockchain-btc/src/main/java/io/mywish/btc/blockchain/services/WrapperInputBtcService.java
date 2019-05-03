package io.mywish.btc.blockchain.services;

import io.mywish.blockchain.WrapperInput;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.*;
import org.bitcoinj.script.Script;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

@Component
@Slf4j
public class WrapperInputBtcService {
    public WrapperInput build(Transaction transaction, TransactionInput input, NetworkParameters networkParameters) {
        String hash = transaction.getHashAsString();
        if (input.isCoinBase()) {
            return new WrapperInput(hash, null, null);
        }

        TransactionOutput connectedOutput = input.getConnectedOutput();
        Script script;
        try {
            script = connectedOutput.getScriptPubKey();
        } catch (ScriptException ex) {
            log.warn("Skip output with script error: {}", connectedOutput, ex);
            return null;
        }
        if (!script.isSentToAddress() && !script.isPayToScriptHash() && !script.isSentToRawPubKey()) {
            log.debug("Skip output with not appropriate script {}.", script);
            return null;
        }
        String address;
        try {
            address = connectedOutput
                    .getScriptPubKey()
                    .getToAddress(networkParameters, true)
                    .toBase58();

        } catch (Exception e) {
            log.error("Impossible to convert script {} to address.", connectedOutput.getScriptPubKey(), e);
            return null;
        }

        return new WrapperInput(
                hash,
                address,
                BigInteger.valueOf(input.getValue().getValue())
        );
    }
}
