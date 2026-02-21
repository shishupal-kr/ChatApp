package com.shishupal.chatapp.dto;

public class TypingDTO {

    private String receiver;

    public TypingDTO() {
    }

    public TypingDTO(String receiver) {
        this.receiver = receiver;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }
}