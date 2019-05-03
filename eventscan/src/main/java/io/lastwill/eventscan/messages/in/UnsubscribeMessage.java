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
public class UnsubscribeMessage extends BaseInMessage {
    public UnsubscribeMessage(UUID id) {
        super(id);
    }
}
