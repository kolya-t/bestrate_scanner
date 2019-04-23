package io.lastwill.eventscan.messages.out;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.lastwill.eventscan.model.Subscription;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.UUID;

@ToString
@Getter
@RequiredArgsConstructor
public abstract class BaseOutMessage {
    @JsonIgnore
    private final Subscription subscription;

    public UUID getId() {
        return subscription.getClientId();
    }

    public abstract String getType();
}
