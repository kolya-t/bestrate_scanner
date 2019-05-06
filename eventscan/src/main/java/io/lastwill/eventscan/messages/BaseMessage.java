package io.lastwill.eventscan.messages;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.lastwill.eventscan.messages.in.SubscribeMessage;
import io.lastwill.eventscan.messages.in.UnsubscribeMessage;
import io.lastwill.eventscan.messages.out.PaymentMessage;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SubscribeMessage.class, name = "subscribe"),
        @JsonSubTypes.Type(value = UnsubscribeMessage.class, name = "unsubscribe"),
        @JsonSubTypes.Type(value = PaymentMessage.class, name = "payment"),
})
public interface BaseMessage {
    @JsonIgnore
    String getType();
}
