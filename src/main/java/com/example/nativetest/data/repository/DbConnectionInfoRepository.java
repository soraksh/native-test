package com.example.nativetest.data.repository;

import com.example.nativetest.data.entity.DbConnectionInfo;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface DbConnectionInfoRepository extends CrudRepository<DbConnectionInfo, UUID> {
    Optional<DbConnectionInfo> findByName(String name);

    Optional<DbConnectionInfo> findById(UUID id);

    void deleteByName(String name);

    void deleteById(UUID id);
}
