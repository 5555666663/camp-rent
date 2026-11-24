package com.camp.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.camp.common.PageResult;
import com.camp.common.Result;
import com.camp.entity.Message;
import com.camp.service.MessageService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 站内消息接口
 *
 * @author camp
 */
@RestController
@RequestMapping("/api/message")
public class MessageController {

    @Resource
    private MessageService messageService;

    /** 我的消息分页 */
    @GetMapping("/page")
    public Result<PageResult<Message>> page(@RequestParam(defaultValue = "1") long current,
                                           @RequestParam(defaultValue = "10") long size,
                                           @RequestParam(required = false) Integer readFlag) {
        IPage<Message> page = messageService.pageMy(current, size, readFlag);
        return Result.success(PageResult.of(page));
    }

    /** 未读数量 */
    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        return Result.success(messageService.unreadCount());
    }

    /** 标记单条已读 */
    @PutMapping("/{id}/read")
    public Result<String> read(@PathVariable Long id) {
        messageService.markRead(id);
        return Result.success("已读");
    }

    /** 全部已读 */
    @PutMapping("/read-all")
    public Result<String> readAll() {
        messageService.readAll();
        return Result.success("全部已读");
    }
}
