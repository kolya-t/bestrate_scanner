package io.lastwill.eventscan.repositories;

import io.lastwill.eventscan.model.Currency;
import io.lastwill.eventscan.model.NetworkType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CurrencyRepository extends CrudRepository<Currency, Long> {
    List<Currency> findAllByNetwork(@Param("network") NetworkType network);
}
