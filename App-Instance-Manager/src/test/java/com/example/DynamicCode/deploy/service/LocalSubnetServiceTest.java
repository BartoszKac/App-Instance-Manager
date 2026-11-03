package com.example.DynamicCode.deploy.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LocalSubnetServiceTest {

    @ParameterizedTest
    @CsvSource({
            "192.168.1.57, 24, 192.168.1.0/24",
            "10.20.30.200, 25, 10.20.30.128/25",
            "172.16.5.9, 30, 172.16.5.8/30"
    })
    void shouldReturnNetworkAddressWhenAddressIsPrivateAndSmallEnough(String ip, int prefix, String expected)
            throws UnknownHostException {
        assertThat(LocalSubnetService.toPrivateCidr(InetAddress.getByName(ip), prefix)).contains(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "8.8.8.8, 24",
            "127.0.0.1, 24",
            "169.254.10.10, 24",
            "10.0.0.5, 16",
            "192.168.1.1, 23",
            "192.168.1.1, 31"
    })
    void shouldSkipAddressWhenPublicOrSubnetOutOfRange(String ip, int prefix) throws UnknownHostException {
        assertThat(LocalSubnetService.toPrivateCidr(InetAddress.getByName(ip), prefix)).isEmpty();
    }

    @Test
    void shouldListConfiguredSubnetAndIgnoreInvalidEntries() {
        LocalSubnetService service = new LocalSubnetService(
                List.of(" 192.168.77.5/24 ", "8.8.8.0/24", "10.0.0.0/16", "--script=x", "example.com/24"));

        assertThat(service.listSubnets())
                .contains("192.168.77.0/24")
                .doesNotContain("8.8.8.0/24", "10.0.0.0/16", "--script=x", "example.com/24");
    }

    @Test
    void shouldSkipIpv6Address() throws UnknownHostException {
        assertThat(LocalSubnetService.toPrivateCidr(InetAddress.getByName("fe80::1"), 64)).isEmpty();
    }
}
