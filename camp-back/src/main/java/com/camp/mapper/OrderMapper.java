package com.camp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.camp.entity.Order;
import com.camp.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 租赁订单 Mapper
 *
 * @author camp
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 分页查询订单列表（附带取件/归还网点名称、用户名、装备名称）
     *
     * @param page        分页参数
     * @param userId      用户ID（用户端查我的订单）
     * @param orderStatus 订单状态（可空）
     * @param storeId     网点ID（管理端按网点筛选，可空）
     */
    @Select("<script>" +
            "SELECT o.*, s1.store_name AS pick_store_name, s2.store_name AS return_store_name, " +
            "u.username, e.eq_name AS equipment_name, e.eq_img " +
            "FROM orders o " +
            "LEFT JOIN store s1 ON o.pick_store_id = s1.id " +
            "LEFT JOIN store s2 ON o.return_store_id = s2.id " +
            "LEFT JOIN user u ON o.user_id = u.id " +
            "LEFT JOIN equipment e ON o.equipment_id = e.id " +
            "WHERE 1=1 " +
            "<if test='userId != null'> AND o.user_id = #{userId} </if>" +
            "<if test='orderStatus != null'> AND o.order_status = #{orderStatus} </if>" +
            "<if test='storeId != null'> AND (o.pick_store_id = #{storeId} OR o.return_store_id = #{storeId}) </if>" +
            "ORDER BY o.id DESC" +
            "</script>")
    IPage<OrderVO> selectOrderPage(Page<OrderVO> page,
                                   @Param("userId") Long userId,
                                   @Param("orderStatus") Integer orderStatus,
                                   @Param("storeId") Long storeId);
}
