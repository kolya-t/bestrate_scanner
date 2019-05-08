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
    private String address;
    @Column(nullable = false)
    private String queueName;
    @Column(nullable = false)
    private Boolean isSubscribed;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NetworkType network;
    private String tokenAddress;
    @Column(nullable = false)
    private String currency;

    public Subscription(UUID clientId,
                        String address,
                        String queueName,
                        boolean isSubscribed,
                        NetworkType network,
                        String tokenAddress,
                        String currency) {
        this.clientId = clientId;
        this.address = address;
        this.queueName = queueName;
        this.isSubscribed = isSubscribed;
        this.network = network;
        this.tokenAddress = tokenAddress;
        this.currency = currency;
    }
}
