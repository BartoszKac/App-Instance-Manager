package com.example.DynamicCode.deploy.dto;

public record ServerCredentialsRequest(String user, String password) {

    @Override
    public String toString() {
        return "ServerCredentialsRequest[user=" + user + "]";
    }
}
