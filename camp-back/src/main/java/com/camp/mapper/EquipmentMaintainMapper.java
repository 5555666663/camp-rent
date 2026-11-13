package com.camp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.camp.entity.EquipmentMaintain;
import com.camp.vo.MaintainVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 装备维保 Mapper
 *
 * @author camp
 */
@Mapper
public interface EquipmentMaintainMapper extends BaseMapper<EquipmentMaintain> {

    /**
     * 分页查询维保记录（附带装备、网点名称）
     */
    @Select("<script>" +
            "SELECT m.*, e.eq_name AS equipment_name, s.store_name AS store_name " +
            "FROM equipment_maintain m " +
            "LEFT JOIN equipment e ON m.equipment_id = e.id " +
            "LEFT JOIN store s ON m.store_id = s.id " +
            "<where>" +
            "  <if test='storeId != null'> AND m.store_id = #{storeId}</if>" +
            "  <if test='equipmentId != null'> AND m.equipment_id = #{equipmentId}</if>" +
            "</where>" +
            " ORDER BY m.id DESC" +
            "</script>")
    IPage<MaintainVO> selectMaintainPage(Page<MaintainVO> page,
                                         @Param("storeId") Long storeId,
                                         @Param("equipmentId") Long equipmentId);
}
