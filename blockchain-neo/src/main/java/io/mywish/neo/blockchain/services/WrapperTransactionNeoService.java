package io.mywish.neo.blockchain.services;

import io.mywish.blockchain.WrapperInput;
import io.mywish.blockchain.WrapperOutput;
import io.mywish.blockchain.WrapperTransaction;
import io.mywish.blockchain.service.WrapperTransactionService;
import io.mywish.neo.blockchain.model.WrapperTransactionNeo;
import io.mywish.neocli4j.Ripemd160;
import io.mywish.neocli4j.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.bind.DatatypeConverter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class WrapperTransactionNeoService implements WrapperTransactionService<Transaction> {
    @Autowired
    private WrapperInputNeoService inputBuilder;

    @Autowired
    private WrapperOutputNeoService outputBuilder;

    private final MessageDigest digest;

    public WrapperTransactionNeoService() throws NoSuchAlgorithmException {
        digest = MessageDigest.getInstance("SHA-256");
    }

    @Override
    public WrapperTransaction build(Transaction transaction) {
        String hash = transaction.getHash();
        List<WrapperInput> inputs = transaction
                .getInputs()
                .stream()
                .map(input -> inputBuilder.build(transaction, input))
                .collect(Collectors.toList());
        List<WrapperOutput> outputs = transaction
                .getOutputs()
                .stream()
                .map(output -> outputBuilder.build(transaction, output))
                .collect(Collectors.toList());
        List<String> contracts = extractContracts(transaction.getScript());
        boolean contractCreation = contracts.isEmpty() && transaction.getType() == Transaction.Type.InvocationTransaction;
        WrapperTransaction res = new WrapperTransactionNeo(
                hash,
                inputs,
                outputs,
                contractCreation,
                transaction.getType(),
                contracts,
                BigInteger.ZERO
        );
        if (contractCreation) {
            String created = extractCreatedContract(transaction.getScript());
            res.setCreates(created);
            // TODO: move it upper
            contracts.add(created);
        }
        return res;
    }

    private List<String> extractContracts(final String scriptHex) {
        if (scriptHex == null) {
            return Collections.emptyList();
        }
        List<String> contracts = new ArrayList<>();
        byte[] script = DatatypeConverter.parseHexBinary(scriptHex);
        for (long i = 0; i < script.length; i++) {
            byte opcode = script[(int)i];
            if (opcode >= 0x01 && opcode <= 0x4B) {
                i += opcode;
                continue;
            }
            if (opcode >= 0x4C && opcode <= 0x4E) {
                i++;
                int toRead = (int)Math.pow(2, opcode - 0x4C) & 0xFF;
                i += toRead;
                long count = 0;
                for (int j = 0; j < toRead; j++) {
                    if (i - j - 1 >= script.length) continue;
                    count = (count << 8) + (script[(int)(i - j - 1)] & 0xFF);
                }
                i += count;
                continue;
            }
            if (opcode >= 0x62 && opcode <= 0x65) {
                i += 3;
                continue;
            }
            if (opcode >= 0x67 && opcode <= 0x69) {
                i++;
                byte[] addressBytes = Arrays.copyOfRange(script, (int)i, (int)(i + 20));
                byte[] address = new byte[addressBytes.length];
                for (int j = 0; j < addressBytes.length; j++) {
                    address[j] = addressBytes[addressBytes.length - j - 1];
                }
                contracts.add("0x" + DatatypeConverter.printHexBinary(address).toLowerCase());
                i += addressBytes.length;
            }
        }

        return contracts;
    }


    private String extractCreatedContract(final String scriptHex) {
        byte[] script = DatatypeConverter.parseHexBinary(scriptHex);
        byte[] contractHash = Ripemd160.getHash(digest.digest(script));
        return "0x" + DatatypeConverter.printHexBinary(contractHash).toLowerCase();
    }
}
