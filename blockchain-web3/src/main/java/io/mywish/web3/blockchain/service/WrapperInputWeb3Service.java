package io.mywish.web3.blockchain.service;

import io.mywish.blockchain.WrapperInput;
import org.springframework.stereotype.Component;
import org.web3j.protocol.core.methods.response.Transaction;

import java.math.BigInteger;

@Component
public class WrapperInputWeb3Service {
    public WrapperInput build(Transaction transaction) {
        return new WrapperInput(
                transaction.getHash(),
                transaction.getFrom(),
                "0".equals(transaction.getValueRaw()) ? BigInteger.ZERO : transaction.getValue()
        );
    }
}
