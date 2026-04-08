package com.zhinengpt.campuscatering.message.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentMessageResponse {

    private Long id;
    private String title;
    private String content;
    private String messageType;
    private String bizType;
    private Long bizId;
    private Integer isRead;
    private LocalDateTime createdAt;
}
