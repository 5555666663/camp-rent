package com.camp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.camp.dto.CommentDTO;
import com.camp.entity.Comment;
import com.camp.entity.Order;
import com.camp.exception.BusinessException;
import com.camp.mapper.CommentMapper;
import com.camp.service.CommentService;
import com.camp.service.OrderService;
import com.camp.util.UserContext;
import com.camp.vo.CommentVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 评价反馈服务实现
 *
 * @author camp
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Resource
    private OrderService orderService;

    /**
     * 新增评价（仅订单完成后开放，关联取件网点）
     */
    @Override
    public void addComment(CommentDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        Order order = orderService.getById(dto.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("只能评价自己的订单");
        }
        if (order.getOrderStatus() == null || order.getOrderStatus() != 3) {
            throw new BusinessException("订单完成后才能评价");
        }
        // 一单一评
        long count = count(new LambdaQueryWrapper<Comment>().eq(Comment::getOrderId, dto.getOrderId()));
        if (count > 0) {
            throw new BusinessException("该订单已评价");
        }
        Comment comment = new Comment();
        comment.setOrderId(dto.getOrderId());
        comment.setUserId(userId);
        comment.setStoreId(order.getPickStoreId());
        comment.setScore(dto.getScore());
        comment.setContent(dto.getContent());
        comment.setCreateTime(LocalDateTime.now());
        save(comment);
    }

    /**
     * 分页查询评价列表
     */
    @Override
    public IPage<CommentVO> pageComments(long current, long size, Long storeId) {
        Page<CommentVO> page = new Page<>(current, size);
        return baseMapper.selectCommentPage(page, storeId);
    }
}
