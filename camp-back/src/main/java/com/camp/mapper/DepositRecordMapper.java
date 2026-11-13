package com.camp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.camp.entity.DepositRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 押金记录 Mapper
 *
 * @author camp
 */
@Mapper
public interface DepositRecordMapper extends BaseMapper<DepositRecord> {
}
