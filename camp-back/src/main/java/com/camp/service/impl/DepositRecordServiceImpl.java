package com.camp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.camp.entity.DepositRecord;
import com.camp.mapper.DepositRecordMapper;
import com.camp.service.DepositRecordService;
import org.springframework.stereotype.Service;

/**
 * 押金记录服务实现
 *
 * @author camp
 */
@Service
public class DepositRecordServiceImpl extends ServiceImpl<DepositRecordMapper, DepositRecord> implements DepositRecordService {
}
