package io.lastwill.eventscan.repositories;

import io.lastwill.eventscan.model.NetworkType;
import io.lastwill.eventscan.model.Subscription;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface SubscriptionRepository extends CrudRepository<Subscription, Long> {
    Subscription findByClientId(@Param("clientId") UUID clientId);

    @Query("select s from Subscription s where s.isSubscribed = true")
    List<Subscription> findAllSubscribed();

    @Query("select s from Subscription s " +
            "where s.isSubscribed = true " +
            "and s.network = :network " +
            "and lower(s.address) in :addresses")
    List<Subscription> findSubscribedByAddressesListAndNetwork(
            @Param("addresses") Collection<String> addresses,
            @Param("network") NetworkType network
    );

    @Query("select s from Subscription s " +
            "where s.isSubscribed = true " +
            "and s.network = :network " +
            "and lower(s.address) = lower(:address)")
    Subscription findSubscribedByAddressAndNetwork(
            @Param("address") String address,
            @Param("network") NetworkType network
    );

    @Query("select s from Subscription s " +
            "where s.isSubscribed = true " +
            "and s.network = :network " +
            "and lower(s.tokenAddress) in :addresses")
    List<Subscription> findSubscribedByTokenAddress(
            @Param("addresses") Collection<String> addresses,
            @Param("network") NetworkType network
    );

    @Query("select s from Subscription s " +
            "where s.isSubscribed = true " +
            "and s.network = :network ")
    List<Subscription> findSubscribedByNetwork(
            @Param("network") NetworkType network
    );
}
