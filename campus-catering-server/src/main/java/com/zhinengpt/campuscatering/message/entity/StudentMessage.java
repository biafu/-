package com.zhinengpt.campuscatering.message.entity;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class StudentMessage {

    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String messageType;
    private String bizType;
    private Long bizId;
    private Integer isRead;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
