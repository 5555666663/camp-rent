package com.camp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.camp.entity.Message;

/**
 * 站内消息服务
 *
 * @author camp
 */
public interface MessageService extends IService<Message> {

    /** 发送消息（按 用户+类型+业务ID 幂等） */
    void send(Long userId, int type, String title, String content, Long bizId);

    /** 分页查询我的消息 */
    IPage<Message> pageMy(long current, long size, Integer readFlag);

    /** 未读数量 */
    long unreadCount();

    /** 标记已读 */
    void markRead(Long id);

    /** 全部已读 */
    void readAll();
}
