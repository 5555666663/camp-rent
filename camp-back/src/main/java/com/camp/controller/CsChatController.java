package com.camp.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.camp.common.PageResult;
import com.camp.common.Result;
import com.camp.entity.ChatMessage;
import com.camp.entity.ChatSession;
import com.camp.service.ChatService;
import com.camp.util.UserContext;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 客服工作台 - 在线聊天接口（会话列表 + 气泡消息 + 回复）
 *
 * @author camp
 */
@RestController
@RequestMapping("/api/cs/chat")
public class CsChatController {

    @Resource
    private ChatService chatService;

    /**
     * 会话分页（按最后消息时间倒序，可用于左侧会话列表）
     */
    @GetMapping("/page")
    public Result<PageResult<ChatSession>> page(@RequestParam(defaultValue = "1") long current,
                                                @RequestParam(defaultValue = "20") long size,
                                                @RequestParam(required = false) String keyword) {
        IPage<ChatSession> page = chatService.pageSessions(current, size, keyword);
        return Result.success(PageResult.of(page));
    }

    /**
     * 某个会话的消息列表（会自动把该会话对「客服」标记为已读）
     */
    @GetMapping("/{id}/messages")
    public Result<List<ChatMessage>> messages(@PathVariable Long id) {
        return Result.success(chatService.messages(id, 2));
    }

    /**
     * 客服回复某个会话
     */
    @PostMapping("/{id}/reply")
    public Result<Void> reply(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Long csId = UserContext.getUserId();
        chatService.csReply(id, csId, "在线客服", body.get("content"));
        return Result.success();
    }

    /**
     * 标记会话已读（客服侧）
     */
    @PutMapping("/{id}/read")
    public Result<Void> read(@PathVariable Long id) {
        chatService.markRead(id, 2);
        return Result.success();
    }
}
