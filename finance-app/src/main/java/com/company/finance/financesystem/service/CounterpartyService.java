package com.company.finance.financesystem.service;

import com.company.finance.financesystem.domain.entity.Counterparty;
import com.company.finance.financesystem.dto.CounterpartyRequest;
import com.company.finance.financesystem.dto.CounterpartyResponse;
import com.company.finance.financesystem.repository.CounterpartyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CounterpartyService {

    private final CounterpartyRepository counterpartyRepository;

    public List<CounterpartyResponse> findAll() {
        return counterpartyRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public CounterpartyResponse create(CounterpartyRequest req) {
        Counterparty c = Counterparty.builder()
                .name(req.name())
                .inn(req.inn())
                .type(req.type())
                .build();
        return toResponse(counterpartyRepository.save(c));
    }

    private CounterpartyResponse toResponse(Counterparty c) {
        return new CounterpartyResponse(c.getId(), c.getName(), c.getInn(), c.getType());
    }
}