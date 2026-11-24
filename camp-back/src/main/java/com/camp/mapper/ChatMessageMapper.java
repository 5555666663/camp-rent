package com.camp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.camp.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客服聊天消息 Mapper
 *
 * @author camp
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}
