package io.mywish.waves.blockchain.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.mywish.blockchain.WrapperInput;
import io.mywish.blockchain.WrapperOutput;
import io.mywish.blockchain.service.WrapperTransactionService;
import io.mywish.waves.blockchain.model.WrapperOutputWaves;
import io.mywish.waves.blockchain.model.WrapperTransactionWaves;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class WrapperTransactionWavesService implements WrapperTransactionService<JsonNode> {
    @Override
    public WrapperTransactionWaves build(JsonNode transaction) {
        String hash = transaction.get("id").asText();
        BigInteger fee = transaction.get("fee").bigIntegerValue();

        List<WrapperInput> inputs = Collections.emptyList();
        List<WrapperOutput> outputs;

        int type = transaction.get("type").asInt();
        // transfer
        String sender = transaction.get("sender").asText();
        if (type == 4) {
            outputs = Collections.singletonList(new WrapperOutputWaves(
                    hash,
                    sender,
                    transaction.get("recipient").asText(),
                    transaction.get("amount").bigIntegerValue(),
                    transaction.get("assetId").asText()
            ));
        }
        // mass transfer
        else if (type == 11) {
            String assetId = transaction.get("assetId").asText();
            int transferCount = transaction.get("transferCount").intValue();
            outputs = new ArrayList<>(transferCount);
            ArrayNode transfers = (ArrayNode) transaction.get("transfers");
            for (JsonNode transfer : transfers) {
                outputs.add(new WrapperOutputWaves(
                        hash,
                        sender,
                        transfer.get("recipient").asText(),
                        transfer.get("amount").bigIntegerValue(),
                        assetId
                ));
            }

        } else {
            outputs = Collections.emptyList();
        }

        return new WrapperTransactionWaves(
                hash,
                inputs,
                outputs,
                fee
        );
    }
}
