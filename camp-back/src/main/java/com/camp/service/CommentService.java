package com.camp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.camp.dto.CommentDTO;
import com.camp.entity.Comment;
import com.camp.vo.CommentVO;

/**
 * 评价反馈服务接口
 *
 * @author camp
 */
public interface CommentService extends IService<Comment> {

    /**
     * 新增评价（订单完成后开放）
     */
    void addComment(CommentDTO dto);

    /**
     * 分页查询评价列表
     */
    IPage<CommentVO> pageComments(long current, long size, Long storeId);
}
