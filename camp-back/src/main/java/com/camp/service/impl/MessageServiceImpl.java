package com.camp.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.camp.entity.Message;
import com.camp.mapper.MessageMapper;
import com.camp.service.MessageService;
import com.camp.util.UserContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 站内消息服务实现
 *
 * @author camp
 */
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Override
    public void send(Long userId, int type, String title, String content, Long bizId) {
        long exists = count(Wrappers.<Message>lambdaQuery()
                .eq(Message::getUserId, userId)
                .eq(Message::getType, type)
                .eq(bizId != null, Message::getBizId, bizId));
        if (exists > 0) {
            return; // 幂等
        }
        Message msg = new Message();
        msg.setUserId(userId);
        msg.setType(type);
        msg.setTitle(title);
        msg.setContent(content);
        msg.setBizId(bizId);
        msg.setReadFlag(0);
        msg.setCreateTime(LocalDateTime.now());
        save(msg);
    }

    @Override
    public IPage<Message> pageMy(long current, long size, Integer readFlag) {
        Long userId = UserContext.getUserId();
        return page(new Page<>(current, size), Wrappers.<Message>lambdaQuery()
                .eq(Message::getUserId, userId)
                .eq(readFlag != null, Message::getReadFlag, readFlag)
                .orderByDesc(Message::getId));
    }

    @Override
    public long unreadCount() {
        Long userId = UserContext.getUserId();
        return count(Wrappers.<Message>lambdaQuery()
                .eq(Message::getUserId, userId).eq(Message::getReadFlag, 0));
    }

    @Override
    public void markRead(Long id) {
        Message msg = getById(id);
        if (msg != null && msg.getUserId().equals(UserContext.getUserId())) {
            msg.setReadFlag(1);
            updateById(msg);
        }
    }

    @Override
    public void readAll() {
        Message update = new Message();
        update.setReadFlag(1);
        update(Wrappers.<Message>lambdaUpdate()
                .eq(Message::getUserId, UserContext.getUserId())
                .eq(Message::getReadFlag, 0)
                .set(Message::getReadFlag, 1));
    }
}
