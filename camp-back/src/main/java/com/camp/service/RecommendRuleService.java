package com.camp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.camp.entity.RecommendRule;

import java.util.List;
import java.util.Map;

/**
 * 推荐规则服务（可视化配置）
 *
 * @author camp
 */
public interface RecommendRuleService extends IService<RecommendRule> {

    /** 启用规则（按优先级升序） */
    List<RecommendRule> listEnabled();

    /** 规则测试：传入模拟天气，返回命中的规则与动作 */
    Map<String, Object> test(Map<String, Object> weather);
}
