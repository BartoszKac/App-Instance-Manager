package com.example.DynamicCode.model.entity.deploy;

import com.example.DynamicCode.constants.deploy.OperationSystem;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class RemoteSerwerConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long IdConfiguration;

    private String name;

    private String ip;

    @Column(name = "username")
    private String user;

    private String pass;

    @Nullable
    @Column(nullable = true)
    private OperationSystem operationSystem;
}
