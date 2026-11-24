package com.camp.controller.admin;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.camp.entity.EquipmentMaintain;
import com.camp.entity.Order;
import com.camp.mapper.StatsMapper;
import com.camp.service.EquipmentMaintainService;
import com.camp.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台：Excel 导出（订单 / 维保 / 统计）
 *
 * @author camp
 */
@RestController
@RequestMapping("/api/admin/export")
public class AdminExportController {

    @Resource
    private OrderService orderService;
    @Resource
    private EquipmentMaintainService maintainService;
    @Resource
    private StatsMapper statsMapper;

    /** 导出订单 */
    @GetMapping("/order")
    public void exportOrder(HttpServletResponse response) throws Exception {
        List<Order> list = orderService.list(Wrappers.<Order>lambdaQuery().orderByDesc(Order::getId));
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Order o : list) {
            Map<String, Object> row = new HashMap<>();
            row.put("订单号", o.getId());
            row.put("用户ID", o.getUserId());
            row.put("装备ID", o.getEquipmentId());
            row.put("数量", o.getQuantity());
            row.put("取件网点", o.getPickStoreId());
            row.put("归还网点", o.getReturnStoreId());
            row.put("租赁开始", o.getRentStartTime());
            row.put("租赁结束", o.getRentEndTime());
            row.put("租金", o.getTotalPrice());
            row.put("押金", o.getDepositMoney());
            row.put("状态", statusText(o.getOrderStatus()));
            row.put("下单时间", o.getCreateTime());
            rows.add(row);
        }
        writeXlsx(response, "订单列表", rows);
    }

    /** 导出维保记录 */
    @GetMapping("/maintain")
    public void exportMaintain(HttpServletResponse response) throws Exception {
        List<EquipmentMaintain> list = maintainService.list();
        List<Map<String, Object>> rows = new ArrayList<>();
        for (EquipmentMaintain m : list) {
            Map<String, Object> row = new HashMap<>();
            row.put("记录ID", m.getId());
            row.put("装备ID", m.getEquipmentId());
            row.put("网点ID", m.getStoreId());
            row.put("维保类型", m.getMaintainType());
            row.put("维保内容", m.getContent());
            row.put("维保时间", m.getMaintainTime());
            row.put("操作人", m.getOperator());
            row.put("备注", m.getRemark());
            rows.add(row);
        }
        writeXlsx(response, "维保记录", rows);
    }

    /** 导出业务统计（3 个 sheet） */
    @GetMapping("/stats")
    public void exportStats(HttpServletResponse response) throws Exception {
        ExcelWriter writer = ExcelUtil.getWriter(true);
        writer.renameSheet("网点订单量");
        writeSheet(writer, "网点", "订单数", statsMapper.countOrderByStore());
        writer.setSheet("装备租赁频次");
        writeSheet(writer, "装备", "租赁次数", statsMapper.countEquipmentRent());
        writer.setSheet("30天订单趋势");
        writeSheet(writer, "日期", "订单数", statsMapper.countOrderTrend(LocalDateTime.now().minusDays(30)));
        output(response, writer, "业务统计");
    }

    private void writeSheet(ExcelWriter writer, String k1, String k2, List<Map<String, Object>> data) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> m : data) {
            Map<String, Object> row = new HashMap<>();
            row.put(k1, m.get("name"));
            row.put(k2, m.get("value"));
            rows.add(row);
        }
        writer.write(rows, true);
    }

    private void writeXlsx(HttpServletResponse response, String sheet, List<Map<String, Object>> rows) throws Exception {
        ExcelWriter writer = ExcelUtil.getWriter(true);
        writer.renameSheet(sheet);
        writer.write(rows, true);
        output(response, writer, sheet);
    }

    private void output(HttpServletResponse response, ExcelWriter writer, String name) throws Exception {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8");
        String fileName = URLEncoder.encode(name + "_" + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")) + ".xlsx", "UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        ServletOutputStream out = response.getOutputStream();
        writer.flush(out, true);
        writer.close();
        out.flush();
        out.close();
    }

    private String statusText(Integer status) {
        if (status == null) {
            return "";
        }
        switch (status) {
            case 0: return "待支付";
            case 1: return "租赁中";
            case 2: return "待归还";
            case 3: return "已完成";
            case 4: return "逾期";
            default: return "未知";
        }
    }
}
