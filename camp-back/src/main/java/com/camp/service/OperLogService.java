package com.camp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.camp.entity.SysOperLog;

/**
 * 操作日志服务
 *
 * @author camp
 */
public interface OperLogService extends IService<SysOperLog> {

    /** 异步保存日志 */
    void saveLog(SysOperLog log);
}
