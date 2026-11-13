package com.camp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.camp.entity.CsMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客服咨询消息 Mapper
 *
 * @author camp
 */
@Mapper
public interface CsMessageMapper extends BaseMapper<CsMessage> {
}
