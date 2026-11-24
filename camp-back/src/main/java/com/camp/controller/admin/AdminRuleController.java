package com.camp.controller.admin;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.camp.annotation.OperLog;
import com.camp.exception.BusinessException;
import com.camp.common.Result;
import com.camp.entity.RecommendRule;
import com.camp.service.RecommendRuleService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 后台：推荐规则可视化配置
 *
 * @author camp
 */
@RestController
@RequestMapping("/api/admin/rule")
public class AdminRuleController {

    @Resource
    private RecommendRuleService ruleService;

    /** 规则列表（按优先级） */
    @GetMapping("/list")
    public Result<List<RecommendRule>> list() {
        return Result.success(ruleService.list(Wrappers.<RecommendRule>lambdaQuery()
                .orderByAsc(RecommendRule::getPriority)));
    }

    /** 新增规则 */
    @OperLog(module = "推荐规则", action = "新增规则")
    @PostMapping
    public Result<String> add(@RequestBody RecommendRule rule) {
        rule.setId(null);
        rule.setEnabled(rule.getEnabled() == null ? 1 : rule.getEnabled());
        rule.setVersion(1);
        rule.setPriority(rule.getPriority() == null ? 100 : rule.getPriority());
        rule.setCreateTime(LocalDateTime.now());
        ruleService.save(rule);
        return Result.success("新增成功");
    }

    /** 编辑规则 */
    @OperLog(module = "推荐规则", action = "编辑规则")
    @PutMapping
    public Result<String> update(@RequestBody RecommendRule rule) {
        RecommendRule old = ruleService.getById(rule.getId());
        if (old == null) {
            throw new BusinessException("规则不存在");
        }
        rule.setVersion(old.getVersion() == null ? 2 : old.getVersion() + 1);
        ruleService.updateById(rule);
        return Result.success("保存成功");
    }

    /** 删除规则 */
    @OperLog(module = "推荐规则", action = "删除规则")
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        ruleService.removeById(id);
        return Result.success("删除成功");
    }

    /** 启用/停用 */
    @OperLog(module = "推荐规则", action = "启停规则")
    @PutMapping("/{id}/status")
    public Result<String> status(@PathVariable Long id, @RequestParam Integer enabled) {
        RecommendRule rule = ruleService.getById(id);
        if (rule == null) {
            throw new BusinessException("规则不存在");
        }
        rule.setEnabled(enabled);
        ruleService.updateById(rule);
        return Result.success(enabled == 1 ? "已启用" : "已停用");
    }

    /** 规则测试：传入模拟天气，返回命中的规则 */
    @PostMapping("/test")
    public Result<Map<String, Object>> test(@RequestBody Map<String, Object> weather) {
        return Result.success(ruleService.test(weather));
    }
}
