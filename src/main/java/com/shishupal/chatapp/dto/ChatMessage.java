package com.shishupal.chatapp.dto;

import lombok.*;

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
    private Boolean edited;
}
