package com.camp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.camp.dto.PackageOrderDTO;
import com.camp.entity.CampPackage;
import com.camp.vo.PackageVO;

import java.util.List;

/**
 * 套餐服务
 * <p>负责套餐查询、套餐一键下单、以及「自己组合套餐」下单。</p>
 *
 * @author camp
 */
public interface CampPackageService extends IService<CampPackage> {

    /**
     * 查询在架套餐列表
     *
     * @param sceneType 场景类型（露营/徒步），为空查全部
     * @return 套餐列表（含装备明细）
     */
    List<PackageVO> listPackages(String sceneType);

    /**
     * 查询套餐详情
     *
     * @param id 套餐ID
     * @return 套餐详情（含装备明细）
     */
    PackageVO getPackageDetail(Long id);

    /**
     * 一键预订整个套餐：套餐内每件装备各生成一条租赁订单
     *
     * @param packageId 套餐ID
     * @param dto       下单参数（网点、起止时间）
     * @return 生成的订单ID列表
     */
    List<Long> orderPackage(Long packageId, PackageOrderDTO dto);

    /**
     * 自己组合套餐下单：按用户勾选的装备清单生成订单
     *
     * @param dto 下单参数（含自定义装备清单）
     * @return 生成的订单ID列表
     */
    List<Long> orderCustom(PackageOrderDTO dto);
}
