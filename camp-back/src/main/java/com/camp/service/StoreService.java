package com.camp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.camp.entity.Store;
import com.camp.vo.StoreDetailVO;
import com.camp.vo.StoreVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 网点服务接口
 *
 * @author camp
 */
public interface StoreService extends IService<Store> {

    /**
     * 查询距离用户最近的网点列表（按距离升序）
     *
     * @param longitude 用户经度
     * @param latitude  用户纬度
     * @return 带距离的网点列表
     */
    List<StoreVO> nearestStores(BigDecimal longitude, BigDecimal latitude);

    /**
     * 查询网点详情（含距离、该网点装备与食品库存）
     *
     * @param storeId   网点ID
     * @param longitude 用户经度（可为空）
     * @param latitude  用户纬度（可为空）
     * @return 网点详情
     */
    StoreDetailVO storeDetail(Long storeId, BigDecimal longitude, BigDecimal latitude);
}
