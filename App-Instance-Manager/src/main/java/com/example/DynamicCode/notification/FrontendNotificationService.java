package com.example.DynamicCode.notification;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class FrontendNotificationService {


    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public final void sendToFrontend(String message) {
        messagingTemplate.convertAndSend("/topic/output", message);
    }
}
