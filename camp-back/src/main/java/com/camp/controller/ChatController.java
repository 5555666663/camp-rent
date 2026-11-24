package com.camp.controller;

import com.camp.common.Result;
import com.camp.entity.ChatMessage;
import com.camp.entity.ChatSession;
import com.camp.service.ChatService;
import com.camp.service.UserService;
import com.camp.util.UserContext;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户端在线客服聊天接口（淘宝式聊天窗口数据源）
 *
 * @author camp
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Resource
    private ChatService chatService;

    @Resource
    private UserService userService;

    /**
     * 取当前用户的会话（不存在则自动创建）
     */
    @GetMapping("/my")
    public Result<ChatSession> my() {
        Long userId = requireUserId();
        return Result.success(chatService.mySession(userId, usernameOf(userId)));
    }

    /**
     * 会话消息列表（会自动把该会话对「用户」标记为已读）
     */
    @GetMapping("/messages")
    public Result<List<ChatMessage>> messages(@RequestParam Long sessionId) {
        requireUserId();
        return Result.success(chatService.messages(sessionId, 1));
    }

    /**
     * 用户发送消息
     */
    @PostMapping("/send")
    public Result<Void> send(@RequestBody Map<String, String> body) {
        Long userId = requireUserId();
        chatService.userSend(userId, usernameOf(userId), body.get("content"));
        return Result.success();
    }

    /** 与前端约定：用户未读数（会话列表/入口红点用） */
    @GetMapping("/unread")
    public Result<Map<String, Object>> unread() {
        Long userId = requireUserId();
        ChatSession session = chatService.mySession(userId, usernameOf(userId));
        Map<String, Object> data = new HashMap<>();
        data.put("unread", session.getUserUnread() == null ? 0 : session.getUserUnread());
        return Result.success(data);
    }

    /** 校验登录并返回用户ID */
    private Long requireUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new com.camp.exception.BusinessException(401, "未登录或登录已过期");
        }
        return userId;
    }

    /** 取用户账号（昵称缺失时兜底） */
    private String usernameOf(Long userId) {
        com.camp.entity.User user = userService.getById(userId);
        return user == null ? "用户" + userId : user.getUsername();
    }
}
