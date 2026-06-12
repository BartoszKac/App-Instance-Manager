package com.example.DynamicCode.model.entity.code;

import com.example.DynamicCode.constants.code.LanguageType;
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
public class CompiledCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCode;

    private String name;

    @Lob
    @Column(name = "code", columnDefinition = "LONGBLOB")
    private byte[] code;

    private LanguageType language;

    private Long idManClass;
}
