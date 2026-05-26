package com.example.DynamicCode.repository.deploy;

import com.example.DynamicCode.model.entity.deploy.RemoteSerwerConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RemoteSerwerRepository extends JpaRepository<RemoteSerwerConfiguration,Long> {

    RemoteSerwerConfiguration findByName(String name);

    void deleteByName(String name);
}
