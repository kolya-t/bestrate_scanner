package io.mywish.neocli4j;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
public class Transaction {
    public enum Type {
        MinerTransaction,
        IssueTransaction,
        ClaimTransaction,
        EnrollmentTransaction,
        RegisterTransaction,
        ContractTransaction,
        AgencyTransaction,
        PublishTransaction,
        InvocationTransaction,
        StateTransaction
    }

    private Type type;
    @JsonProperty("txid")
    private String hash;
    @JsonProperty("vout")
    private List<TransactionOutput> outputs;

    @JsonProperty("vin")
    private List<TransactionInput> inputs;
    private List<String> contracts;

    private String script;

    @JsonProperty("net_fee")
    private BigDecimal fee;

    public Transaction() {
        this.contracts = new ArrayList<>();
    }
}
