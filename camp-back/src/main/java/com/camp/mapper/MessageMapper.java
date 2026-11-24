package com.camp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.camp.entity.Message;
import org.apache.ibatis.annotations.Mapper;

/**
 * 站内消息 Mapper
 *
 * @author camp
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
