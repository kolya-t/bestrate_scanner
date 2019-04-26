package io.lastwill.eventscan.messages.in;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
public class SubscribeMessage extends BaseInMessage {
    private String tcrAddress;
    private String queueName;

    public SubscribeMessage(UUID id, String tcrAddress, String queueName) {
        super(id);
        this.tcrAddress = tcrAddress;
        this.queueName = queueName;
    }
}
