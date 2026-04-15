package com.theMs.sakany.access.internal.infrastructure.persistence;

import com.theMs.sakany.access.internal.domain.AccessCode;
import com.theMs.sakany.access.internal.domain.AccessCodeRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class AccessCodeRepositoryImpl implements AccessCodeRepository {

    private final AccessCodeJpaRepository jpaRepository;
    private final AccessCodeMapper mapper;

    public AccessCodeRepositoryImpl(AccessCodeJpaRepository jpaRepository, AccessCodeMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void save(AccessCode accessCode) {
        AccessCodeEntity entity = mapper.toEntity(accessCode);
        jpaRepository.save(entity);
    }

    @Override
    public Optional<AccessCode> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<AccessCode> findByCode(String code) {
        return jpaRepository.findByCode(code).map(mapper::toDomain);
    }

    @Override
    public List<AccessCode> findByResidentId(UUID residentId) {
        return jpaRepository.findByResidentId(residentId)
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }
}
