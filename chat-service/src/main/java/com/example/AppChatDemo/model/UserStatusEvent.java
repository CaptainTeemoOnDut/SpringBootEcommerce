package com.example.AppChatDemo.model;

import lombok.*;


@Setter
@Getter
public class UserStatusEvent {
    private String userId;
    private String status; // "ONLINE" or "OFFLINE"
    private long timestamp;

    public UserStatusEvent(String userId, String status, long timestamp) {
        this.userId = userId;
        this.status = status;
        this.timestamp = timestamp;
    }

}


