package com.example.DynamicCode.deploy.service;

import java.util.ArrayList;
import java.util.List;

final class NmapCommands {

    private static final String PROBED_PORTS = "22,53,80,135,139,443,445,554,631,1883,3389,5000,5900,8080,8443,9100,62078";
    private static final String DETAIL_SCRIPTS = "nbstat,http-title,smb-os-discovery,ssl-cert";

    private NmapCommands() {
    }

    static List<String> sweep(String cidr) {
        return List.of("nmap", "--privileged", "-sn", "-PE", "-n", "-T4", "-oX", "-", cidr);
    }

    static List<String> openPorts(List<String> hosts) {
        List<String> command = new ArrayList<>(List.of(
                "nmap", "-Pn", "-sT", "-n", "-T4", "--max-retries", "1", "-p", PROBED_PORTS, "--open", "-oX", "-"));
        command.addAll(hosts);
        return command;
    }

    static List<String> details(String ip) {
        return List.of("nmap", "-Pn", "-sT", "-sV", "--version-light", "-n", "-T4", "--host-timeout", "90s",
                "-p", PROBED_PORTS, "--open", "--script", DETAIL_SCRIPTS, "--script-timeout", "10s", "-oX", "-", ip);
    }
}
