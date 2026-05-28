package com.example.DynamicCode.model.entity.deploy;

import com.example.DynamicCode.constants.code.LanguageType;
import com.example.DynamicCode.constants.deploy.UploadStrategyType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Builder
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

    private UploadStrategyType uploadStrategyType;

    private String pathInServer;

    private Long idCode;

}
