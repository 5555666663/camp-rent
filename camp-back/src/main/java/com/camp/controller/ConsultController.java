package com.camp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.camp.common.PageResult;
import com.camp.common.Result;
import com.camp.entity.CsMessage;
import com.camp.entity.User;
import com.camp.service.CsMessageService;
import com.camp.service.UserService;
import com.camp.util.UserContext;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 用户端咨询服务接口（必须登录）
 * 普通用户只能提交、只能查看自己的咨询记录
 *
 * @author camp
 */
@RestController
@RequestMapping("/api/consult")
public class ConsultController {

    @Resource
    private CsMessageService csMessageService;

    @Resource
    private UserService userService;

    /**
     * 提交咨询
     *
     * @param body title 咨询标题，content 咨询内容
     */
    @PostMapping
    public Result<Long> submit(@RequestBody Map<String, String> body) {
        // 1. 登录校验：未登录直接拒绝
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "请先登录后再提交咨询");
        }
        String title = body.get("title");
        String content = body.get("content");
        // 2. 参数校验
        if (title == null || title.trim().isEmpty()) {
            return Result.error("请填写咨询标题");
        }
        if (content == null || content.trim().isEmpty()) {
            return Result.error("请填写咨询内容");
        }
        if (title.trim().length() > 100) {
            return Result.error("咨询标题不能超过 100 个字符");
        }
        if (content.trim().length() > 1000) {
            return Result.error("咨询内容不能超过 1000 个字符");
        }
        // 3. 组装并保存
        User user = userService.getById(userId);
        CsMessage msg = new CsMessage();
        msg.setUserId(userId);
        msg.setUsername(user == null ? ("用户" + userId) : user.getUsername());
        msg.setTitle(title.trim());
        msg.setContent(content.trim());
        msg.setStatus(0);
        msg.setCreateTime(LocalDateTime.now());
        csMessageService.save(msg);
        return Result.success(msg.getId());
    }

    /**
     * 我的咨询记录分页（只返回当前登录用户自己的数据）
     */
    @GetMapping("/my")
    public Result<PageResult<CsMessage>> myPage(@RequestParam(defaultValue = "1") long current,
                                                @RequestParam(defaultValue = "10") long size) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        LambdaQueryWrapper<CsMessage> wrapper = new LambdaQueryWrapper<>();
        // 关键：按当前登录用户过滤，保证普通用户看不到别人的咨询
        wrapper.eq(CsMessage::getUserId, userId).orderByDesc(CsMessage::getId);
        return Result.success(PageResult.of(csMessageService.page(new Page<>(current, size), wrapper)));
    }
}
