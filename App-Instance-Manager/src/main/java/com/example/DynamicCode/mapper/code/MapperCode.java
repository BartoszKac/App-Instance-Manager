package com.example.DynamicCode.mapper.code;


import com.example.DynamicCode.model.entity.code.SourceCode;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MapperCode {

    public List<String> toFileNameList(List<SourceCode> sourceCodes) {
        if (sourceCodes == null || sourceCodes.isEmpty()) {
            return Collections.emptyList();
        }

        return sourceCodes.stream()
                .map(com.example.DynamicCode.model.entity.code.SourceCode::getName)
                .collect(Collectors.toList());
    }
}