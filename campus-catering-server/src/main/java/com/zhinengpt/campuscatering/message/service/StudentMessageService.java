package com.zhinengpt.campuscatering.message.service;

import com.zhinengpt.campuscatering.message.dto.StudentMessageResponse;
import com.zhinengpt.campuscatering.message.entity.StudentMessage;
import com.zhinengpt.campuscatering.message.mapper.StudentMessageMapper;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class StudentMessageService {

    private final StudentMessageMapper studentMessageMapper;

    public StudentMessageService(StudentMessageMapper studentMessageMapper) {
        this.studentMessageMapper = studentMessageMapper;
    }

    public void push(Long userId, String title, String content, String messageType, String bizType, Long bizId) {
        StudentMessage studentMessage = new StudentMessage();
        studentMessage.setUserId(userId);
        studentMessage.setTitle(title);
        studentMessage.setContent(content);
        studentMessage.setMessageType(messageType);
        studentMessage.setBizType(bizType);
        studentMessage.setBizId(bizId);
        studentMessage.setIsRead(0);
        studentMessageMapper.insert(studentMessage);
    }

    public List<StudentMessageResponse> list(Long userId, boolean onlyUnread, Integer limit) {
        int safeLimit = Math.max(1, Math.min(limit == null ? 50 : limit, 200));
        return studentMessageMapper.selectLatest(userId, onlyUnread ? 1 : 0, safeLimit).stream()
                .map(message -> StudentMessageResponse.builder()
                        .id(message.getId())
                        .title(message.getTitle())
                        .content(message.getContent())
                        .messageType(message.getMessageType())
                        .bizType(message.getBizType())
                        .bizId(message.getBizId())
                        .isRead(message.getIsRead())
                        .createdAt(message.getCreatedAt())
                        .build())
                .toList();
    }

    public void markRead(Long userId, Long messageId) {
        studentMessageMapper.markRead(messageId, userId);
    }

    public void markAllRead(Long userId) {
        studentMessageMapper.markAllRead(userId);
    }

    public Long unreadCount(Long userId) {
        return studentMessageMapper.countUnread(userId);
    }
}
