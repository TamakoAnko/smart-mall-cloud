package com.xzj.dto;

import org.springframework.ai.chat.messages.Message;

public record ChatMessageView(String role, String text) {

    public static ChatMessageView from(Message message) {
        return new ChatMessageView(message.getMessageType().getValue(),
                message.getText() == null ? "" : message.getText());
    }
}