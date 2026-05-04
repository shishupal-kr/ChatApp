package com.shishupal.chatapp.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    private String sender;
    private String receiver;
    private String content;
    private String status;
    private Long id;
    private LocalDateTime timestamp;
    private Boolean edited;
    private Long replyToId;
    private String replyToSender;
    private String replyToContent;
}
