package com.example.DynamicCode.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProcesDefault{
    private ArrayList<String> processes = new ArrayList<>();
    private String mainProcess;

    public void addProcess(String process) {
        processes.add(process);
    }


}


