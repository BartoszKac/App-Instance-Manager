package com.example.DynamicCode.model.entity.deploy;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
}
