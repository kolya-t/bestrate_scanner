package io.lastwill.eventscan.messages.in;

import io.lastwill.eventscan.messages.BaseMessage;
import io.lastwill.eventscan.model.NetworkType;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class SubscribeMessage implements BaseMessage {
    private final String type = "subscribe";
    private UUID id;
    private String address;
    private String queueName;
    private String tokenAddress;
    private String currency;
    private NetworkType blockchain;
}
