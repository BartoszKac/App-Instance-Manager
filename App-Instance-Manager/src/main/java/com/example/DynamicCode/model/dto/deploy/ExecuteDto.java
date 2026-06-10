package com.example.DynamicCode.model.dto.deploy;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ExecuteDto {

    private String command;
    private Long programConfigId;
}
