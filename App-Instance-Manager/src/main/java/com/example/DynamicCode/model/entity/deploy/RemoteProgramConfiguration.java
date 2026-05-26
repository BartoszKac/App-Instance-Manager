package com.example.DynamicCode.model.entity.deploy;

import com.example.DynamicCode.constants.code.LanguageType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class RemoteProgramConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idConfiguration;

    private Long idSerwer;

    private LanguageType language;

    private String pathInServer;

    private Long idCode;

}
