package com.example.DynamicCode.deploy.service;

public record RemoteTarget(String ip, String user, String password, String directory) {

    @Override
    public String toString() {
        return "RemoteTarget[ip=" + ip + ", user=" + user + ", directory=" + directory + "]";
    }
}
