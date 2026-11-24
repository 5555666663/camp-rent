package com.camp.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.camp.entity.RecommendRule;
import com.camp.mapper.RecommendRuleMapper;
import com.camp.service.RecommendRuleService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 推荐规则服务实现（JSON 条件解释执行）
 *
 * @author camp
 */
@Service
public class RecommendRuleServiceImpl extends ServiceImpl<RecommendRuleMapper, RecommendRule> implements RecommendRuleService {

    @Override
    public List<RecommendRule> listEnabled() {
        return list(Wrappers.<RecommendRule>lambdaQuery()
                .eq(RecommendRule::getEnabled, 1)
                .orderByAsc(RecommendRule::getPriority));
    }

    @Override
    public Map<String, Object> test(Map<String, Object> weather) {
        List<RecommendRule> rules = listEnabled();
        Map<String, Object> result = new HashMap<>();
        for (RecommendRule rule : rules) {
            if (match(rule.getConditionJson(), weather)) {
                result.put("matched", true);
                result.put("ruleId", rule.getId());
                result.put("ruleName", rule.getRuleName());
                result.put("action", rule.getActionJson());
                return result;
            }
        }
        result.put("matched", false);
        result.put("ruleName", "默认安全策略");
        result.put("action", "{\"riskLevel\":\"low\",\"sceneType\":\"camping\"}");
        return result;
    }

    /** 判断条件 JSON 是否命中（字段间 AND） */
    @SuppressWarnings("unchecked")
    public static boolean match(String conditionJson, Map<String, Object> weather) {
        if (conditionJson == null || conditionJson.trim().isEmpty() || weather == null) {
            return false;
        }
        Object parsed = cn.hutool.json.JSONUtil.parse(conditionJson);
        if (!(parsed instanceof cn.hutool.json.JSONObject)) {
            return false;
        }
        cn.hutool.json.JSONObject cond = (cn.hutool.json.JSONObject) parsed;
        for (String key : cond.keySet()) {
            Object ruleVal = cond.get(key);
            Object fact = weather.get(key);
            if (fact == null) {
                return false;
            }
            if (ruleVal instanceof cn.hutool.json.JSONObject) {
                cn.hutool.json.JSONObject cmp = (cn.hutool.json.JSONObject) ruleVal;
                double f;
                try {
                    f = Double.parseDouble(String.valueOf(fact));
                } catch (NumberFormatException nfe) {
                    // 布尔/文本型事实（如 thunder=true）走字符串比较
                    boolean allEq = true;
                    for (String op : cmp.keySet()) {
                        if (!"eq".equals(op) || !String.valueOf(fact).equalsIgnoreCase(String.valueOf(cmp.get(op)))) {
                            allEq = false;
                            break;
                        }
                    }
                    if (!allEq) {
                        return false;
                    }
                    continue;
                }
                for (String op : cmp.keySet()) {
                    double target = Double.parseDouble(String.valueOf(cmp.get(op)));
                    boolean ok;
                    switch (op) {
                        case "gte": ok = f >= target; break;
                        case "gt": ok = f > target; break;
                        case "lte": ok = f <= target; break;
                        case "lt": ok = f < target; break;
                        case "eq": ok = Math.abs(f - target) < 1e-9 || String.valueOf(fact).equalsIgnoreCase(String.valueOf(cmp.get(op))); break;
                        default: ok = false;
                    }
                    if (!ok) {
                        return false;
                    }
                }
            } else {
                String a = String.valueOf(fact);
                String b = String.valueOf(ruleVal);
                if (!a.equalsIgnoreCase(b)) {
                    return false;
                }
            }
        }
        return true;
    }
}
