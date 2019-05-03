package io.mywish.eos.blockchain.model;

import io.mywish.blockchain.ContractEventDefinition;
import lombok.Getter;

import java.util.List;

@Getter
public class EosActionFieldsDefinition extends ContractEventDefinition {
    private final List<String> fields;

    public EosActionFieldsDefinition(String name, List<String> fields) {
        super(name);
        this.fields = fields;
    }
}
