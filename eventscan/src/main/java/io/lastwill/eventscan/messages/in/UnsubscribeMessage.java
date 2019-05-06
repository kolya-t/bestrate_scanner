package io.lastwill.eventscan.messages.in;

import io.lastwill.eventscan.messages.BaseMessage;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
public class UnsubscribeMessage implements BaseMessage {
    private final String type = "unsubscribe";
    private UUID id;
}
