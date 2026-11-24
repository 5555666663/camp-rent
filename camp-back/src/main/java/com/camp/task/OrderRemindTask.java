package com.camp.task;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.camp.entity.Order;
import com.camp.service.FoodBatchService;
import com.camp.service.MessageService;
import com.camp.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务：租期到期提醒、逾期标记、食品批次状态刷新
 *
 * @author camp
 */
@Slf4j
@Component
public class OrderRemindTask {

    @Resource
    private OrderService orderService;
    @Resource
    private MessageService messageService;
    @Resource
    private FoodBatchService foodBatchService;

    /** 每天 09:00：24 小时内到期提醒 */
    @Scheduled(cron = "0 0 9 * * ?")
    public void remindDueSoon() {
        LocalDateTime now = LocalDateTime.now();
        List<Order> list = orderService.list(Wrappers.<Order>lambdaQuery()
                .in(Order::getOrderStatus, 1, 2)
                .ge(Order::getRentEndTime, now)
                .le(Order::getRentEndTime, now.plusHours(24)));
        for (Order o : list) {
            messageService.send(o.getUserId(), 1, "租期即将到期",
                    "订单 #" + o.getId() + " 将于 " + o.getRentEndTime() + " 到期，请按时归还装备。", o.getId());
        }
        log.info("到期提醒任务完成，共 {} 单", list.size());
    }

    /** 每小时：逾期订单置为逾期并提醒 */
    @Scheduled(cron = "0 5 * * * ?")
    public void markOverdue() {
        LocalDateTime now = LocalDateTime.now();
        List<Order> list = orderService.list(Wrappers.<Order>lambdaQuery()
                .in(Order::getOrderStatus, 1, 2)
                .lt(Order::getRentEndTime, now));
        for (Order o : list) {
            o.setOrderStatus(4);
            orderService.updateById(o);
            messageService.send(o.getUserId(), 2, "订单已逾期",
                    "订单 #" + o.getId() + " 已超过归还时间，请尽快归还，逾期将影响押金退还。", o.getId());
        }
        if (!list.isEmpty()) {
            log.info("逾期标记任务完成，共 {} 单", list.size());
        }
    }

    /** 每天 08:30：刷新食品批次状态（临期/过期/售罄） */
    @Scheduled(cron = "0 30 8 * * ?")
    public void refreshFoodBatch() {
        foodBatchService.refreshStatus();
    }
}
