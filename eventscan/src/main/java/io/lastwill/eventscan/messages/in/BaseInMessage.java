package io.lastwill.eventscan.messages.in;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.UUID;

@Getter
@ToString
@RequiredArgsConstructor
public abstract class BaseInMessage {
    private final UUID id;
}
