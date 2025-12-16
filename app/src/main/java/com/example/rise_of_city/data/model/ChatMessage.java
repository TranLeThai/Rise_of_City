package com.example.rise_of_city.data.model;

public class ChatMessage {
    public enum MessageType {
        USER,
        AI
    }
    
    private String message;
    private MessageType type;
    private long timestamp;
    private boolean isLoading;
    
    public ChatMessage(String message, MessageType type) {
        this.message = message;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
        this.isLoading = false;
    }
    
    public ChatMessage(String message, MessageType type, boolean isLoading) {
        this.message = message;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
        this.isLoading = isLoading;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public MessageType getType() {
        return type;
    }
    
    public void setType(MessageType type) {
        this.type = type;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public boolean isLoading() {
        return isLoading;
    }
    
    public void setLoading(boolean loading) {
        isLoading = loading;
    }
}

