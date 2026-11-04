package com.example.DynamicCode.inspection.service;

import com.example.DynamicCode.inspection.entity.InspectionHostKey;
import com.example.DynamicCode.inspection.repository.InspectionHostKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InspectionHostKeys {

    private final InspectionHostKeyRepository hostKeys;

    public String pinnedFor(Long serverId, String ip) {
        return hostKeys.findById(serverId)
                .filter(key -> key.getIp().equals(ip))
                .map(InspectionHostKey::getFingerprint)
                .orElse("");
    }

    public void pin(Long serverId, String ip, String fingerprint) {
        if (!fingerprint.isEmpty()) {
            hostKeys.save(new InspectionHostKey(serverId, ip, fingerprint));
        }
    }
}
