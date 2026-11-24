package com.camp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.camp.entity.SysOperLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志 Mapper
 *
 * @author camp
 */
@Mapper
public interface SysOperLogMapper extends BaseMapper<SysOperLog> {
}
