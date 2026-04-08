package com.example.DynamicCode.model;

import java.util.ArrayList;

public class ProcesDefault{
    private ArrayList<String> processes = new ArrayList<>();
    private String mainProcess;

    public void addProcess(String process) {
        processes.add(process);
    }

    public ArrayList<String> getProcesses() {
        return processes;
    }

    public void setProcesses(ArrayList<String> processes) {
        this.processes = processes;
    }

    public String getMainProcess() {
        return mainProcess;
    }

    public void setMainProcess(String mainProcess) {
        this.mainProcess = mainProcess;
    }

    public ProcesDefault() {
    }

    public ProcesDefault(ArrayList<String> processes, String mainProcess) {
        this.processes = processes;
        this.mainProcess = mainProcess;
    }
}


