package com.camp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.camp.entity.Payment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付单 Mapper
 *
 * @author camp
 */
@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {
}
