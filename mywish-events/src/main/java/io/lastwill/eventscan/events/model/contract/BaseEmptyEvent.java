package io.lastwill.eventscan.events.model.contract;


public class BaseEmptyEvent extends ContractEvent {
    public BaseEmptyEvent(ContractEventDefinition definition, String address) {
        super(definition, address);
    }
}