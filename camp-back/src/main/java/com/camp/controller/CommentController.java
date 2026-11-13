package com.camp.controller;

import com.camp.common.Result;
import com.camp.dto.CommentDTO;
import com.camp.service.CommentService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 评价反馈接口（用户端）
 *
 * @author camp
 */
@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @Resource
    private CommentService commentService;

    /**
     * 新增评价（订单完成后开放）
     */
    @PostMapping
    public Result<Void> add(@Valid @RequestBody CommentDTO dto) {
        commentService.addComment(dto);
        return Result.success("评价成功", null);
    }
}
