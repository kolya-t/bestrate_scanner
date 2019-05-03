package io.lastwill.eventscan.model;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@Entity
@EqualsAndHashCode
@Table(name = "currency")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Currency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String symbol;
    @Column(nullable = false)
    private String tokenAddress;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NetworkType network;
}
