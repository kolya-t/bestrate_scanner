package io.lastwill.eventscan.messages.in;

import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@Getter
@ToString(callSuper = true)
public class SubscribeMessage extends BaseInMessage {
    private final String tcrAddress;
    private final String queueName;

    public SubscribeMessage(UUID id, String tcrAddress, String queueName) {
        super(id);
        this.tcrAddress = tcrAddress;
        this.queueName = queueName;
    }
}
