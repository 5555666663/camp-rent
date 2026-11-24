package com.camp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.camp.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客服会话 Mapper
 *
 * @author camp
 */
@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {
}
