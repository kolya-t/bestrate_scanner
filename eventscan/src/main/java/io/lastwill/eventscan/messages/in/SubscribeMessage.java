package io.lastwill.eventscan.messages.in;

import io.lastwill.eventscan.model.NetworkType;
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
    private String address;
    private String queueName;
    private NetworkType network;

    public SubscribeMessage(UUID id, String address, String queueName, NetworkType network) {
        super(id);
        this.address = address;
        this.queueName = queueName;
        this.network = network;
    }
}
