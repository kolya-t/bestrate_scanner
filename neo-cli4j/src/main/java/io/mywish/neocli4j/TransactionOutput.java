package io.mywish.neocli4j;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class TransactionOutput {
    @JsonProperty("n")
    private Integer index;
    private String address;
    private String asset;
    private BigDecimal value;
}
