package io.lastwill.eventscan.messages.in;

import io.lastwill.eventscan.messages.BaseMessage;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseInMessage implements BaseMessage {
    private UUID id;
}
