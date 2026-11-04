package com.example.inspector.ssh;

public record SshLogin(String host, String user, String password) {

    @Override
    public String toString() {
        return "SshLogin[" + host + "]";
    }
}
