package com.example.DynamicCode.deploy.repository;

import com.example.DynamicCode.deploy.entity.RemoteSerwerConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RemoteSerwerRepository extends JpaRepository<RemoteSerwerConfiguration,Long> {

    RemoteSerwerConfiguration findByName(String name);

    void deleteByName(String name);
}
