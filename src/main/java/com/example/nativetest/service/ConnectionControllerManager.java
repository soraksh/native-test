package com.example.nativetest.service;

import com.example.nativetest.data.entity.DbConnectionInfo;
import com.example.nativetest.data.repository.DbConnectionInfoRepository;
import com.example.nativetest.rest.exception.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.transaction.annotation.Transactional;

@Component(ConnectionControllerManager.NAME)
public class ConnectionControllerManager {
    public static final String NAME = "ConnectionControllerManager";

    @Autowired
    protected DbConnectionInfoRepository repository;

    public Iterable<DbConnectionInfo> findAll() {
        return repository.findAll();
    }

    public DbConnectionInfo create(DbConnectionInfo connectionInfo) {
        return repository.save(connectionInfo);
    }

    public DbConnectionInfo findOne(String name) {
        return repository.findByName(name).orElseThrow(() ->
                new EntityNotFoundException("Database connection info", name));
    }

    public DbConnectionInfo updateOne(String name, DbConnectionInfo connectionInfo) {
        DbConnectionInfo foundConnectionInfo = repository.findByName(name).orElse(new DbConnectionInfo());
        foundConnectionInfo.setName(connectionInfo.getName());
        foundConnectionInfo.setDatabaseName(connectionInfo.getDatabaseName());
        foundConnectionInfo.setHostname(connectionInfo.getHostname());
        foundConnectionInfo.setUsername(connectionInfo.getUsername());
        foundConnectionInfo.setPassword(connectionInfo.getPassword());
        foundConnectionInfo.setPort(connectionInfo.getPort());
        foundConnectionInfo = repository.save(foundConnectionInfo);
        return foundConnectionInfo;
    }

    @Transactional
    public void delete(@PathVariable String name) {
        repository.deleteByName(name);
    }
}
