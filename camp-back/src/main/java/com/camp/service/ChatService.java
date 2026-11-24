package com.camp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.camp.entity.ChatMessage;
import com.camp.entity.ChatSession;

import java.util.List;

/**
 * 在线客服聊天服务
 *
 * @author camp
 */
public interface ChatService {

    /** 取（或创建）当前用户的会话 */
    ChatSession mySession(Long userId, String username);

    /** 会话消息列表（按时间升序），并可标记某一方已读 */
    List<ChatMessage> messages(Long sessionId, Integer viewerType);

    /** 用户发送消息 */
    void userSend(Long userId, String username, String content);

    /** 客服会话分页（按最后消息时间倒序） */
    IPage<ChatSession> pageSessions(long current, long size, String keyword);

    /** 客服回复消息 */
    void csReply(Long sessionId, Long csId, String csName, String content);

    /** 标记会话对某一方已读：1 用户 2 客服 */
    void markRead(Long sessionId, Integer viewerType);
}
