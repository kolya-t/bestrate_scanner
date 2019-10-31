package io.mywish.neocli4j;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
public class TransactionInput {
    @Setter
    private TransactionOutput connectedOutput;
    private String txid;
    private Integer vout;
}
