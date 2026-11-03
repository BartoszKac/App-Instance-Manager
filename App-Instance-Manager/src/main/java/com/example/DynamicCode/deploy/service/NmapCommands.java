package com.example.DynamicCode.deploy.service;

import java.util.ArrayList;
import java.util.List;

final class NmapCommands {

    private static final String PORT_SCRIPTS = String.join(",",
            "banner", "http-title", "http-headers", "http-server-header", "http-generator", "http-robots.txt",
            "http-methods", "http-ntlm-info", "ssl-cert", "ssh-hostkey", "rdp-ntlm-info", "rdp-enum-encryption",
            "vnc-info", "rtsp-methods", "cups-info", "mysql-info", "redis-info", "ms-sql-ntlm-info");
    private static final String HOST_SCRIPTS = String.join(",",
            "nbstat", "smb-os-discovery", "smb-protocols", "smb2-security-mode", "smb2-time");

    private NmapCommands() {
    }

    static List<String> sweep(List<String> hosts) {
        return withTargets(List.of("nmap", "--privileged", "-sn", "-PE", "-n", "-T4", "-oX", "-"), hosts);
    }

    static List<String> openPorts(String ip) {
        return List.of("nmap", "-Pn", "-sT", "-n", "-T4", "--max-retries", "1", "--max-rtt-timeout", "800ms",
                "--min-parallelism", "32", "--top-ports", "1000", "--open", "--host-timeout", "60s", "-oX", "-", ip);
    }

    static List<String> names(List<String> hosts, List<String> dnsServers) {
        return withTargets(List.of("nmap", "-sL", "-R", "--dns-servers", String.join(",", dnsServers), "-oX", "-"), hosts);
    }

    static List<String> portDetails(String ip, int port) {
        return List.of("nmap", "-Pn", "-sT", "-sV", "--version-light", "-n", "-T4", "--host-timeout", "75s",
                "-p", String.valueOf(port), "--script", PORT_SCRIPTS, "--script-timeout", "15s", "-oX", "-", ip);
    }

    static List<String> hostDetails(String ip, int port) {
        return List.of("nmap", "-Pn", "-sT", "-n", "-T4", "--host-timeout", "45s",
                "-p", String.valueOf(port), "--script", HOST_SCRIPTS, "--script-timeout", "15s", "-oX", "-", ip);
    }

    private static List<String> withTargets(List<String> base, List<String> hosts) {
        List<String> command = new ArrayList<>(base);
        command.addAll(hosts);
        return command;
    }
}
