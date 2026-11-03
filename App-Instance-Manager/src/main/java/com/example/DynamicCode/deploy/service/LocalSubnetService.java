package com.example.DynamicCode.deploy.service;

import com.example.DynamicCode.deploy.exception.NetworkScanFailedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Slf4j
@Service
public class LocalSubnetService {

    private static final int MIN_PREFIX = 24;
    private static final int MAX_PREFIX = 30;
    private static final Pattern IPV4_CIDR = Pattern.compile("(\\d{1,3}(?:\\.\\d{1,3}){3})/(\\d{1,2})");

    private final List<String> configuredSubnets;

    public LocalSubnetService(@Value("${aim.network.scan-subnets:}") List<String> configuredSubnets) {
        this.configuredSubnets = configuredSubnets;
    }

    public List<String> listSubnets() {
        try {
            return Stream.concat(detectSubnets(), configuredSubnets())
                    .distinct()
                    .sorted()
                    .toList();
        } catch (SocketException e) {
            throw new NetworkScanFailedException("Nie udało się odczytać kart sieciowych tego komputera.", e);
        }
    }

    static Optional<String> toPrivateCidr(InetAddress address, int prefix) {
        boolean privateIpv4 = address instanceof Inet4Address && address.isSiteLocalAddress();
        if (!privateIpv4 || prefix < MIN_PREFIX || prefix > MAX_PREFIX) {
            return Optional.empty();
        }
        int network = ByteBuffer.wrap(address.getAddress()).getInt() & (-1 << (32 - prefix));
        return Optional.of(formatIpv4(network) + "/" + prefix);
    }

    static Optional<String> parseConfiguredSubnet(String value) {
        Matcher matcher = IPV4_CIDR.matcher(value.trim());
        if (!matcher.matches()) {
            return Optional.empty();
        }
        try {
            return toPrivateCidr(InetAddress.getByName(matcher.group(1)), Integer.parseInt(matcher.group(2)));
        } catch (UnknownHostException e) {
            return Optional.empty();
        }
    }

    private Stream<String> detectSubnets() throws SocketException {
        return NetworkInterface.networkInterfaces()
                .filter(this::isScannable)
                .flatMap(networkInterface -> networkInterface.getInterfaceAddresses().stream())
                .map(address -> toPrivateCidr(address.getAddress(), address.getNetworkPrefixLength()))
                .flatMap(Optional::stream);
    }

    private Stream<String> configuredSubnets() {
        return configuredSubnets.stream()
                .map(this::parseOrWarn)
                .flatMap(Optional::stream);
    }

    private Optional<String> parseOrWarn(String value) {
        Optional<String> subnet = parseConfiguredSubnet(value);
        if (subnet.isEmpty()) {
            log.warn("Ignoring aim.network.scan-subnets entry that is not a private /24-/30 subnet: {}", value);
        }
        return subnet;
    }

    private static String formatIpv4(int value) {
        return String.format("%d.%d.%d.%d",
                value >>> 24, (value >>> 16) & 255, (value >>> 8) & 255, value & 255);
    }

    private boolean isScannable(NetworkInterface networkInterface) {
        try {
            return networkInterface.isUp() && !networkInterface.isLoopback() && !networkInterface.isVirtual();
        } catch (SocketException e) {
            return false;
        }
    }
}
