package io.lastwill.eventscan.services.handlers;

import io.lastwill.eventscan.events.model.SubscriptionPaymentEvent;
import io.lastwill.eventscan.messages.out.PaymentMessage;
import io.lastwill.eventscan.services.MQProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnBean(MQProducer.class)
public class PaymentEventDispatcher {
    @Autowired
    private MQProducer producer;

    @EventListener
    private void onSubscriptionPayment(final SubscriptionPaymentEvent event) {
        try {
            producer.send(
                    new PaymentMessage(
                            event.getSubscription(),
                            event.getNetworkType(),
                            event.getTransaction().getHash(),
                            event.getCurrency(),
                            event.getAmount()
                    )
            );
        } catch (Throwable e) {
            log.error("Sending notification about payment failed.", e);
        }
    }
}
