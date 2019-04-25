package io.lastwill.eventscan.messages.in;

import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@Getter
@ToString(callSuper = true)
public class UnsubscribeMessage extends BaseInMessage {
    public UnsubscribeMessage(UUID id) {
        super(id);
    }
}
