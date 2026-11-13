package com.camp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.camp.entity.CampPackageItem;
import com.camp.vo.PackageItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 套餐装备明细 Mapper
 *
 * @author camp
 */
@Mapper
public interface CampPackageItemMapper extends BaseMapper<CampPackageItem> {

    /**
     * 查询套餐内装备明细（联查装备表，带出装备名称、图片、价格、库存、所属网点）
     *
     * @param packageId 套餐ID
     * @return 套餐装备明细列表
     */
    @Select("SELECT i.package_id, i.equipment_id, i.quantity, e.eq_name, e.eq_img, e.price_per_day, " +
            "e.deposit, e.stock_num, e.store_id " +
            "FROM camp_package_item i LEFT JOIN equipment e ON i.equipment_id = e.id " +
            "WHERE i.package_id = #{packageId} ORDER BY i.id ASC")
    List<PackageItemVO> selectItems(@Param("packageId") Long packageId);
}
