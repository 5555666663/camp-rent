package com.camp.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.camp.common.PageResult;
import com.camp.common.Result;
import com.camp.service.CommentService;
import com.camp.vo.CommentVO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 评价反馈管理接口（管理员）
 *
 * @author camp
 */
@RestController
@RequestMapping("/api/admin/comment")
public class AdminCommentController {

    @Resource
    private CommentService commentService;

    /**
     * 评价分页列表（支持网点筛选）
     */
    @GetMapping("/page")
    public Result<PageResult<CommentVO>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long storeId) {
        IPage<CommentVO> page = commentService.pageComments(current, size, storeId);
        return Result.success(PageResult.of(page));
    }

    /**
     * 删除不良评价
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        commentService.removeById(id);
        return Result.success("删除成功", null);
    }
}
