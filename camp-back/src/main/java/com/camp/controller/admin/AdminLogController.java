package com.camp.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.camp.common.PageResult;
import com.camp.common.Result;
import com.camp.entity.SysOperLog;
import com.camp.service.OperLogService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 后台：系统操作日志
 *
 * @author camp
 */
@RestController
@RequestMapping("/api/admin/log")
public class AdminLogController {

    @Resource
    private OperLogService operLogService;

    /** 日志分页（按模块/操作人筛选） */
    @GetMapping("/page")
    public Result<PageResult<SysOperLog>> page(@RequestParam(defaultValue = "1") long current,
                                              @RequestParam(defaultValue = "10") long size,
                                              @RequestParam(required = false) String module,
                                              @RequestParam(required = false) String username) {
        IPage<SysOperLog> page = operLogService.page(new Page<>(current, size),
                Wrappers.<SysOperLog>lambdaQuery()
                        .like(module != null && !module.isEmpty(), SysOperLog::getModule, module)
                        .like(username != null && !username.isEmpty(), SysOperLog::getUsername, username)
                        .orderByDesc(SysOperLog::getId));
        return Result.success(PageResult.of(page));
    }

    /** 日志详情 */
    @GetMapping("/{id}")
    public Result<SysOperLog> detail(@PathVariable Long id) {
        return Result.success(operLogService.getById(id));
    }
}
