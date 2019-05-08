package io.lastwill.eventscan.messages.out;

import io.lastwill.eventscan.messages.BaseMessage;
import io.lastwill.eventscan.model.NetworkType;
import lombok.*;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMessage implements BaseMessage {
    private final String type = "payment";
    private UUID id;
    private NetworkType blockchain;
    private String blockHash;
    private String hash;
    private String addressFrom;
    private String addressTo;
    private Instant timestamp;
    private String tokenAddress;
    private String currency;
    private BigInteger amount;
    private BigInteger fee;
    private String description;
}
