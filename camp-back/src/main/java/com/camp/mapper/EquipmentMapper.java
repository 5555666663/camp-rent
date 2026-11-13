package com.camp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.camp.entity.Equipment;
import com.camp.vo.EquipmentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 露营装备 Mapper
 *
 * @author camp
 */
@Mapper
public interface EquipmentMapper extends BaseMapper<Equipment> {

    /**
     * 分页查询装备列表（附带网点、分类名称）
     *
     * @param page        分页参数
     * @param storeId     网点ID（可空）
     * @param categoryId  分类ID（可空）
     * @param keyword     关键词（可空）
     * @param status      状态（可空，1-上架 0-下架）
     * @param productType 商品类型（可空，1-装备 2-食品）
     */
    @Select("<script>" +
            "SELECT e.*, s.store_name, c.category_name FROM equipment e " +
            "LEFT JOIN store s ON e.store_id = s.id " +
            "LEFT JOIN category c ON e.category_id = c.id " +
            "WHERE 1=1 " +
            "<if test='storeId != null'> AND e.store_id = #{storeId} </if>" +
            "<if test='categoryId != null'> AND e.category_id = #{categoryId} </if>" +
            "<if test='productType != null'> AND e.product_type = #{productType} </if>" +
            "<if test='status != null'> AND e.status = #{status} </if>" +
            "<if test='keyword != null and keyword != \"\"'> AND (e.eq_name LIKE CONCAT('%', #{keyword}, '%') OR e.brand LIKE CONCAT('%', #{keyword}, '%')) </if>" +
            "ORDER BY e.id DESC" +
            "</script>")
    IPage<EquipmentVO> selectEquipmentPage(Page<EquipmentVO> page,
                                           @Param("storeId") Long storeId,
                                           @Param("categoryId") Long categoryId,
                                           @Param("keyword") String keyword,
                                           @Param("status") Integer status,
                                           @Param("productType") Integer productType);

    /**
     * 原子扣减库存（条件更新，库存不足时返回 0）
     */
    @Update("UPDATE equipment SET stock_num = stock_num - #{num} WHERE id = #{id} AND stock_num >= #{num}")
    int deductStock(@Param("id") Long id, @Param("num") int num);

    /**
     * 恢复库存
     */
    @Update("UPDATE equipment SET stock_num = stock_num + #{num} WHERE id = #{id}")
    int restoreStock(@Param("id") Long id, @Param("num") int num);
}
