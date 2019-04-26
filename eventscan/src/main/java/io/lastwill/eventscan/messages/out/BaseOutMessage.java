package io.lastwill.eventscan.messages.out;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.lastwill.eventscan.messages.BaseMessage;
import io.lastwill.eventscan.model.Subscription;
import lombok.*;

import java.util.UUID;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseOutMessage implements BaseMessage {
    @JsonIgnore
    private Subscription subscription;

    public UUID getId() {
        return subscription.getClientId();
    }
}
