package io.lastwill.eventscan.messages.in;

import lombok.ToString;

@ToString
public abstract class BaseInMessage {

    public abstract String getType();
}
