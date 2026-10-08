package com.company.finance.financesystem.repository;

import com.company.finance.financesystem.domain.entity.Counterparty;
import com.company.finance.financesystem.domain.enums.CounterpartyType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CounterpartyRepository extends JpaRepository<Counterparty, Long> {
    List<Counterparty> findAllByType(CounterpartyType type);
    List<Counterparty> findAllByInn(String inn);
}