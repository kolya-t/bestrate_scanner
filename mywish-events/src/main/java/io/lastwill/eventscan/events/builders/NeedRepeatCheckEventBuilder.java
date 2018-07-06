package io.lastwill.eventscan.events.builders;

import io.mywish.wrapper.ContractEventBuilder;
import io.mywish.wrapper.WrapperType;
import io.lastwill.eventscan.events.model.contract.CheckedEvent;
import io.mywish.wrapper.ContractEventDefinition;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.web3j.abi.datatypes.Bool;

import java.util.Collections;
import java.util.List;

@Getter
@Component
public class NeedRepeatCheckEventBuilder extends ContractEventBuilder<CheckedEvent> {
    private final ContractEventDefinition definition = new ContractEventDefinition(
            "NeedRepeatCheck",
            Collections.singletonList(WrapperType.create(Bool.class, false))
    );

    @Override
    public CheckedEvent build(String address, List<Object> values) {
        return new CheckedEvent(
                definition,
                (Boolean) values.get(0),
                address
        );
    }
}
