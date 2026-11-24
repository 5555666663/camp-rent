package com.camp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.camp.entity.SysOperLog;
import com.camp.mapper.SysOperLogMapper;
import com.camp.service.OperLogService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 操作日志服务实现
 *
 * @author camp
 */
@Service
public class OperLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLog> implements OperLogService {

    @Override
    @Async
    public void saveLog(SysOperLog log) {
        if (log.getCreateTime() == null) {
            log.setCreateTime(LocalDateTime.now());
        }
        save(log);
    }
}
