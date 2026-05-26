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
public class SourceCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String code;

    private LanguageType language;

    private Long idManClass;

}