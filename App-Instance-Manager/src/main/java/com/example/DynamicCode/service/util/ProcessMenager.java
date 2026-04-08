package com.example.DynamicCode.service.util;

import com.example.DynamicCode.model.ProcesDefault;
import org.springframework.stereotype.Component;
import java.util.ArrayList;

@Component
public class ProcessMenager {

    private ArrayList<ProcesDefault> procesDefaults = new ArrayList<>();

    public ArrayList<ProcesDefault> getProcesDefaults() {
        return procesDefaults;
    }

    public void setProcesDefaults(ArrayList<ProcesDefault> procesDefaults) {
        this.procesDefaults = procesDefaults;
    }

    public void addProcesDefault(ProcesDefault procesDefault) {
        // Usuwamy stary wpis o tej samej nazwie głównej przed dodaniem nowego
        this.procesDefaults.removeIf(p -> p.getMainProcess().equals(procesDefault.getMainProcess()));
        this.procesDefaults.add(procesDefault);
    }

    public ProcesDefault getProcesDefaultByMainProcess(String mainProcess) {
        return procesDefaults.stream()
                .filter(p -> p.getMainProcess().equals(mainProcess))
                .findFirst()
                .orElse(null);
    }

    public ProcessMenager() {
    }
}