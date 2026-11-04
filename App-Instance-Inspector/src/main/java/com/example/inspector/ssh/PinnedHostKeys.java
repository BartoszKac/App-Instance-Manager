package com.example.inspector.ssh;

import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.UserInfo;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class PinnedHostKeys implements HostKeyRepository {

    private final String pinned;
    private String seenKey = "";
    private boolean mismatch;

    public PinnedHostKeys(String pinned) {
        this.pinned = pinned;
    }

    public String seenKey() {
        return seenKey;
    }

    public boolean isMismatch() {
        return mismatch;
    }

    @Override
    public int check(String host, byte[] key) {
        seenKey = fingerprint(key);
        mismatch = !pinned.isEmpty() && !pinned.equals(seenKey);
        return mismatch ? CHANGED : OK;
    }

    @Override
    public void add(HostKey hostkey, UserInfo ui) {
    }

    @Override
    public void remove(String host, String type) {
    }

    @Override
    public void remove(String host, String type, byte[] key) {
    }

    @Override
    public String getKnownHostsRepositoryID() {
        return "pinned-host-keys";
    }

    @Override
    public HostKey[] getHostKey() {
        return new HostKey[0];
    }

    @Override
    public HostKey[] getHostKey(String host, String type) {
        return new HostKey[0];
    }

    private static String fingerprint(byte[] key) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(key);
            return "SHA256:" + Base64.getEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
