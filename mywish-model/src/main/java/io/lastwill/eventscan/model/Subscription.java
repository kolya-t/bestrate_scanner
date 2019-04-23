package io.lastwill.eventscan.model;

import lombok.*;

import javax.persistence.*;
import java.util.UUID;

@Getter
@Setter
@Entity
@EqualsAndHashCode
@Table(name = "subscription")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private UUID clientId;
    @Column(nullable = false)
    private String tcrAddress;
    @Column(nullable = false)
    private String queueName;
    @Column(nullable = false)
    private Boolean isSubscribed;

    public Subscription(UUID clientId, String tcrAddress, String queueName, boolean isSubscribed) {
        this.clientId = clientId;
        this.tcrAddress = tcrAddress;
        this.queueName = queueName;
        this.isSubscribed = isSubscribed;
    }
}
