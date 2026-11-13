package com.camp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.camp.entity.Equipment;
import com.camp.service.AiRecommendService;
import com.camp.service.EquipmentService;
import com.camp.vo.PackageItemVO;
import com.camp.vo.PackageVO;
import com.camp.vo.RecommendVO;
import com.camp.vo.WeatherVO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AI 套餐推荐服务实现
 * <p>调用 OpenAI 兼容协议的大模型（DeepSeek / 通义千问 / 智谱 等均可，改 base-url 与 model 即可）。
 * 在 application.yml 中配置：</p>
 * <pre>
 * ai:
 *   enabled: true
 *   base-url: https://api.deepseek.com
 *   api-key: 你的Key
 *   model: deepseek-chat
 * </pre>
 * <p>未配置 Key 或调用异常时，自动回退到规则引擎，保证功能可用。</p>
 *
 * @author camp
 */
@Slf4j
@Service
public class AiRecommendServiceImpl implements AiRecommendService {

    /** 是否启用大模型 */
    @Value("${ai.enabled:false}")
    private boolean aiEnabled;

    /** 大模型服务地址（OpenAI 兼容） */
    @Value("${ai.base-url:https://api.deepseek.com}")
    private String aiBaseUrl;

    /** 大模型 API Key */
    @Value("${ai.api-key:}")
    private String aiApiKey;

    /** 大模型名称 */
    @Value("${ai.model:deepseek-chat}")
    private String aiModel;

    /** 大模型调用超时（毫秒） */
    @Value("${ai.timeout:20000}")
    private int aiTimeout;

    @Resource
    private EquipmentService equipmentService;

    @Override
    public RecommendVO recommend(LocalDate date, String location, WeatherVO weather, List<PackageVO> packages) {
        AiResult result = null;
        if (aiEnabled && StrUtil.isNotBlank(aiApiKey)) {
            try {
                result = callAi(date, location, weather, packages);
            } catch (Exception e) {
                log.warn("大模型调用失败，回退规则引擎：{}", e.getMessage());
            }
        }
        boolean fromAi = result != null;
        if (!fromAi) {
            result = ruleRecommend(weather, packages);
        }

        RecommendVO vo = new RecommendVO();
        vo.setRiskLevel(result.getRiskLevel());
        vo.setRiskTip(result.getRiskTip());
        vo.setSceneType(result.getSceneType());
        vo.setRemindText(result.getRemindText());
        vo.setRecommendPackage(resolvePackages(result.getRecommendPackage(), result.getSceneType(), packages));
        vo.setOptionalAdd(resolveEquipment(result.getOptionalAdd()));
        vo.setWeather(weather);
        vo.setDate(date.toString());
        vo.setLocation(location);
        vo.setSource(fromAi ? "ai" : "rule");
        return vo;
    }

    // ==================== 大模型调用 ====================

    /**
     * 调用大模型生成推荐
     */
    private AiResult callAi(LocalDate date, String location, WeatherVO weather, List<PackageVO> packages) {
        String system = "你是「露营装备租赁系统」的智能装备推荐助手。你只能输出一个 JSON 对象，不要输出任何解释文字、不要使用 Markdown 代码块。";
        String user = buildPrompt(date, location, weather, packages);

        JSONObject body = JSONUtil.createObj();
        body.set("model", aiModel);
        body.set("temperature", 0.7);
        JSONArray messages = JSONUtil.createArray();
        messages.add(JSONUtil.createObj().set("role", "system").set("content", system));
        messages.add(JSONUtil.createObj().set("role", "user").set("content", user));
        body.set("messages", messages);

        String resp = HttpRequest.post(aiBaseUrl + "/chat/completions")
                .header("Authorization", "Bearer " + aiApiKey)
                .header("Content-Type", "application/json")
                .body(body.toString())
                .timeout(aiTimeout)
                .execute()
                .body();
        if (StrUtil.isBlank(resp)) {
            throw new IllegalStateException("大模型返回为空");
        }
        JSONObject json = JSONUtil.parseObj(resp);
        JSONArray choices = json.getJSONArray("choices");
        if (choices == null || choices.isEmpty()) {
            throw new IllegalStateException("大模型返回无 choices");
        }
        String content = choices.getJSONObject(0).getJSONObject("message").getStr("content");
        JSONObject out = JSONUtil.parseObj(cleanJson(content));

        AiResult r = new AiResult();
        r.setRiskLevel(out.getStr("risk_level", "注意"));
        r.setRiskTip(out.getStr("risk_tip", ""));
        r.setSceneType(out.getStr("scene_type", "露营"));
        r.setRecommendPackage(toStringList(out.getJSONArray("recommend_package")));
        r.setOptionalAdd(toStringList(out.getJSONArray("optional_add")));
        r.setRemindText(out.getStr("remind_text", ""));
        return r;
    }

    /**
     * 构造提示词：日期 + 地点 + 天气 + 套餐列表 + 输出规则
     */
    private String buildPrompt(LocalDate date, String location, WeatherVO weather, List<PackageVO> packages) {
        StringBuilder sb = new StringBuilder();
        sb.append("请根据以下信息推荐露营/徒步装备套餐：\n");
        sb.append("【出行日期】").append(date).append("（未来7天内）\n");
        sb.append("【出行地点】").append(location).append("\n");
        sb.append("【当日天气】").append(weather.getWeatherText())
                .append("，气温 ").append(weather.getTempMin()).append("~").append(weather.getTempMax()).append("℃")
                .append("，降水概率 ").append(weather.getPrecipitationProbability()).append("%")
                .append("，是否下雨 ").append(Boolean.TRUE.equals(weather.getWillRain()) ? "是" : "否")
                .append("，最大风力 ").append(weather.getWindSpeed()).append("km/h")
                .append("，紫外线指数 ").append(weather.getUvIndex())
                .append("，雷电预警 ").append(Boolean.TRUE.equals(weather.getThunderAlert()) ? "有" : "无")
                .append("\n");
        sb.append("【系统套餐列表】\n");
        for (PackageVO p : packages) {
            sb.append("- ").append(p.getPackageName())
                    .append("（场景：").append(p.getSceneType())
                    .append("，日租价：").append(p.getPrice()).append("元）包含：");
            List<String> names = new ArrayList<>();
            if (p.getItems() != null) {
                for (PackageItemVO item : p.getItems()) {
                    names.add(item.getEqName() + "×" + item.getQuantity());
                }
            }
            sb.append(String.join("、", names)).append("\n");
        }
        sb.append("\n【输出规则】\n");
        sb.append("1. risk_level 只能是「安全」「注意」「高危」三者之一；\n");
        sb.append("2. 若雷电预警为「有」、或降水概率≥70%、或最大风力≥40km/h，risk_level 必须为「高危」，risk_tip 必须先给出明确的风险警告并建议改期；\n");
        sb.append("3. scene_type 只能是「露营」或「徒步」，需结合天气与地点判断；\n");
        sb.append("4. recommend_package 从上面套餐名称中选 1-2 个，必须与 scene_type 匹配；\n");
        sb.append("5. optional_add 是建议补充携带的装备名称（1-3 个，尽量与天气相关）；\n");
        sb.append("6. remind_text 是给用户的出行提醒，30 字以内；\n");
        sb.append("7. 严格输出如下 JSON（不要代码块、不要多余文字）：\n");
        sb.append("{\"risk_level\":\"\",\"risk_tip\":\"\",\"scene_type\":\"\",\"recommend_package\":[\"\"],\"optional_add\":[\"\"],\"remind_text\":\"\"}");
        return sb.toString();
    }

    /**
     * 去掉 ```json 代码块包裹
     */
    private String cleanJson(String content) {
        if (content == null) {
            return "{}";
        }
        String s = content.trim();
        int i = s.indexOf('{');
        int j = s.lastIndexOf('}');
        if (i >= 0 && j > i) {
            s = s.substring(i, j + 1);
        }
        return s;
    }

    /**
     * JSON 数组转字符串列表
     */
    private List<String> toStringList(JSONArray array) {
        List<String> list = new ArrayList<>();
        if (array != null) {
            for (Object o : array) {
                if (o != null) {
                    list.add(o.toString());
                }
            }
        }
        return list;
    }

    // ==================== 规则引擎（兜底） ====================

    /**
     * 本地规则推荐：天气高危优先风险警告
     */
    private AiResult ruleRecommend(WeatherVO w, List<PackageVO> packages) {
        boolean thunder = Boolean.TRUE.equals(w.getThunderAlert());
        boolean rain = Boolean.TRUE.equals(w.getWillRain());
        int pp = w.getPrecipitationProbability() == null ? 0 : w.getPrecipitationProbability();
        double wind = w.getWindSpeed() == null ? 0 : w.getWindSpeed();
        double uv = w.getUvIndex() == null ? 0 : w.getUvIndex();
        double tempMax = w.getTempMax() == null ? 25 : w.getTempMax();

        AiResult r = new AiResult();
        if (thunder || pp >= 70 || wind >= 40) {
            r.setRiskLevel("高危");
            r.setRiskTip("天气存在雷电/强降雨/大风风险，不建议在户外长时间露营，建议改期或改为短途徒步，并远离高处、水域和孤立树木。");
            r.setSceneType("徒步");
            r.setRemindText("建议改期；若必须出行请携带雨具、注意防雷。");
        } else if (rain || pp >= 40) {
            r.setRiskLevel("注意");
            r.setRiskTip("当日有降雨可能，露营体验会受影响，建议优先选择短途徒步并携带雨具与地布。");
            r.setSceneType("徒步");
            r.setRemindText("带好雨具，选择排水良好的营地。");
        } else if (uv >= 8) {
            r.setRiskLevel("注意");
            r.setRiskTip("紫外线较强，长时间户外易晒伤，建议早晚出行并做好防晒。");
            r.setSceneType("露营");
            r.setRemindText("备好防晒霜、遮阳帽和墨镜。");
        } else if (tempMax >= 33) {
            r.setRiskLevel("注意");
            r.setRiskTip("气温偏高，注意补水防暑，避免正午长时间暴晒。");
            r.setSceneType("徒步");
            r.setRemindText("多带饮用水，避开正午出行。");
        } else {
            r.setRiskLevel("安全");
            r.setRiskTip("天气条件良好，温度适宜，适合露营与徒步。");
            r.setSceneType("露营");
            r.setRemindText("天气不错，注意早晚温差。");
        }

        List<String> scenePkgs = new ArrayList<>();
        for (PackageVO p : packages) {
            if (r.getSceneType().equals(p.getSceneType())) {
                scenePkgs.add(p.getPackageName());
            }
        }
        if (scenePkgs.isEmpty()) {
            for (PackageVO p : packages) {
                scenePkgs.add(p.getPackageName());
            }
        }
        r.setRecommendPackage(scenePkgs.size() > 2 ? scenePkgs.subList(0, 2) : scenePkgs);

        // 附加装备：雨/雷电 -> 照明+炊具；否则桌椅+灯串
        if (thunder || rain) {
            r.setOptionalAdd(List.of("LED露营灯", "便携卡式炉"));
        } else {
            r.setOptionalAdd(List.of("折叠桌椅套装", "营地灯串"));
        }
        return r;
    }

    // ==================== 结果解析 ====================

    /**
     * 套餐名 -> 套餐对象
     */
    private List<PackageVO> resolvePackages(List<String> names, String sceneType, List<PackageVO> packages) {
        List<PackageVO> result = new ArrayList<>();
        if (names != null) {
            for (String name : names) {
                for (PackageVO p : packages) {
                    if (p.getPackageName().equals(name) || (name != null && name.contains(p.getPackageName()))) {
                        if (!result.contains(p)) {
                            result.add(p);
                        }
                    }
                }
            }
        }
        if (result.isEmpty()) {
            for (PackageVO p : packages) {
                if (sceneType != null && sceneType.equals(p.getSceneType())) {
                    result.add(p);
                }
            }
        }
        if (result.isEmpty()) {
            result.addAll(packages.size() > 2 ? packages.subList(0, 2) : packages);
        }
        // 最多返回 2 个套餐
        return result.size() > 2 ? result.subList(0, 2) : result;
    }

    /**
     * 装备名 -> 装备对象（最多 3 个）
     */
    private List<Equipment> resolveEquipment(List<String> names) {
        List<Equipment> all = equipmentService.list(new LambdaQueryWrapper<Equipment>()
                .eq(Equipment::getStatus, 1).last("LIMIT 50"));
        List<Equipment> result = new ArrayList<>();
        if (names != null) {
            for (String name : names) {
                for (Equipment e : all) {
                    if (e.getEqName().equals(name) || (name != null && name.contains(e.getEqName()))) {
                        if (!result.contains(e)) {
                            result.add(e);
                        }
                    }
                }
            }
        }
        if (result.isEmpty()) {
            result.addAll(all.size() > 3 ? all.subList(0, 3) : all);
        }
        return result.size() > 3 ? result.subList(0, 3) : result;
    }

    /**
     * 大模型/规则引擎的中间结果
     */
    @Data
    private static class AiResult {
        /** 风险等级 */
        private String riskLevel;
        /** 风险提示 */
        private String riskTip;
        /** 场景类型 */
        private String sceneType;
        /** 推荐套餐名称 */
        private List<String> recommendPackage = Collections.emptyList();
        /** 附加装备名称 */
        private List<String> optionalAdd = Collections.emptyList();
        /** 提醒文案 */
        private String remindText;
    }
}
