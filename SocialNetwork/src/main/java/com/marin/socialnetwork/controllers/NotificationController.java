package com.marin.socialnetwork.controllers;

import com.marin.socialnetwork.models.Notification;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class NotificationController {

    @MessageMapping("/sendNotification")
    @SendTo("/topic/notifications")
    public Notification send(Notification notification) {
        return notification;
    }
}
