package com.camp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.camp.entity.ChatMessage;
import com.camp.entity.ChatSession;
import com.camp.exception.BusinessException;
import com.camp.mapper.ChatMessageMapper;
import com.camp.mapper.ChatSessionMapper;
import com.camp.service.ChatService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 在线客服聊天服务实现
 *
 * @author camp
 */
@Service
public class ChatServiceImpl implements ChatService {

    @Resource
    private ChatSessionMapper chatSessionMapper;

    @Resource
    private ChatMessageMapper chatMessageMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatSession mySession(Long userId, String username) {
        ChatSession session = chatSessionMapper.selectOne(new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getUserId, userId));
        if (session == null) {
            session = new ChatSession();
            session.setUserId(userId);
            session.setUsername(username);
            session.setUserUnread(0);
            session.setCsUnread(0);
            session.setStatus(0);
            session.setCreateTime(LocalDateTime.now());
            session.setLastTime(LocalDateTime.now());
            session.setLastContent("会话已创建，请描述您的问题");
            chatSessionMapper.insert(session);
        }
        return session;
    }

    @Override
    public List<ChatMessage> messages(Long sessionId, Integer viewerType) {
        if (viewerType != null) {
            markRead(sessionId, viewerType);
        }
        return chatMessageMapper.selectList(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId)
                .orderByAsc(ChatMessage::getId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void userSend(Long userId, String username, String content) {
        if (!StringUtils.hasText(content)) {
            throw new BusinessException("消息内容不能为空");
        }
        ChatSession session = mySession(userId, username);
        saveMessage(session.getId(), 1, userId, username, content);
        // 用户发消息 -> 客服未读 +1
        chatSessionMapper.update(null, new LambdaUpdateWrapper<ChatSession>()
                .eq(ChatSession::getId, session.getId())
                .set(ChatSession::getLastContent, content)
                .set(ChatSession::getLastTime, LocalDateTime.now())
                .setSql("cs_unread = cs_unread + 1"));
    }

    @Override
    public IPage<ChatSession> pageSessions(long current, long size, String keyword) {
        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<ChatSession>()
                .orderByDesc(ChatSession::getLastTime);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(ChatSession::getUsername, keyword)
                    .or().like(ChatSession::getLastContent, keyword));
        }
        return chatSessionMapper.selectPage(new Page<>(current, size), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void csReply(Long sessionId, Long csId, String csName, String content) {
        if (!StringUtils.hasText(content)) {
            throw new BusinessException("回复内容不能为空");
        }
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        saveMessage(sessionId, 2, csId, csName, content);
        // 客服回复 -> 用户未读 +1
        chatSessionMapper.update(null, new LambdaUpdateWrapper<ChatSession>()
                .eq(ChatSession::getId, sessionId)
                .set(ChatSession::getLastContent, content)
                .set(ChatSession::getLastTime, LocalDateTime.now())
                .setSql("user_unread = user_unread + 1"));
    }

    @Override
    public void markRead(Long sessionId, Integer viewerType) {
        if (viewerType == null || sessionId == null) {
            return;
        }
        LambdaUpdateWrapper<ChatSession> wrapper = new LambdaUpdateWrapper<ChatSession>()
                .eq(ChatSession::getId, sessionId);
        if (viewerType == 1) {
            wrapper.set(ChatSession::getUserUnread, 0);
        } else {
            wrapper.set(ChatSession::getCsUnread, 0);
        }
        chatSessionMapper.update(null, wrapper);
    }

    /** 写入一条聊天消息 */
    private void saveMessage(Long sessionId, Integer senderType, Long senderId, String senderName, String content) {
        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setSenderType(senderType);
        message.setSenderId(senderId);
        message.setSenderName(senderName);
        message.setContent(content);
        message.setCreateTime(LocalDateTime.now());
        chatMessageMapper.insert(message);
    }
}
