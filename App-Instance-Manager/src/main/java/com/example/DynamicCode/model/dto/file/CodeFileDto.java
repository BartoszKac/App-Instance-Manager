package com.example.DynamicCode.model.dto.file;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CodeFileDto {

   private String nameFile;
   private byte[] fileContent;
}
