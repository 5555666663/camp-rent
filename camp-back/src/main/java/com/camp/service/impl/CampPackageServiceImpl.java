package com.camp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.camp.dto.OrderCreateDTO;
import com.camp.dto.PackageOrderDTO;
import com.camp.entity.CampPackage;
import com.camp.exception.BusinessException;
import com.camp.mapper.CampPackageItemMapper;
import com.camp.mapper.CampPackageMapper;
import com.camp.service.CampPackageService;
import com.camp.service.OrderService;
import com.camp.vo.PackageItemVO;
import com.camp.vo.PackageVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 套餐服务实现
 *
 * @author camp
 */
@Service
public class CampPackageServiceImpl extends ServiceImpl<CampPackageMapper, CampPackage>
        implements CampPackageService {

    @Resource
    private CampPackageItemMapper campPackageItemMapper;

    @Resource
    private OrderService orderService;

    @Override
    public List<PackageVO> listPackages(String sceneType) {
        LambdaQueryWrapper<CampPackage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CampPackage::getStatus, 1);
        if (StrUtil.isNotBlank(sceneType)) {
            wrapper.eq(CampPackage::getSceneType, sceneType);
        }
        wrapper.orderByAsc(CampPackage::getId);

        List<PackageVO> result = new ArrayList<>();
        for (CampPackage pkg : list(wrapper)) {
            PackageVO vo = new PackageVO();
            BeanUtil.copyProperties(pkg, vo);
            vo.setItems(campPackageItemMapper.selectItems(pkg.getId()));
            result.add(vo);
        }
        return result;
    }

    @Override
    public PackageVO getPackageDetail(Long id) {
        CampPackage pkg = getById(id);
        if (pkg == null) {
            return null;
        }
        PackageVO vo = new PackageVO();
        BeanUtil.copyProperties(pkg, vo);
        vo.setItems(campPackageItemMapper.selectItems(id));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> orderPackage(Long packageId, PackageOrderDTO dto) {
        PackageVO pkg = getPackageDetail(packageId);
        if (pkg == null) {
            throw new BusinessException("套餐不存在");
        }
        if (pkg.getItems() == null || pkg.getItems().isEmpty()) {
            throw new BusinessException("该套餐暂未配置装备");
        }
        List<PackageOrderDTO.Item> items = new ArrayList<>();
        for (PackageItemVO item : pkg.getItems()) {
            PackageOrderDTO.Item i = new PackageOrderDTO.Item();
            i.setEquipmentId(item.getEquipmentId());
            i.setQuantity(item.getQuantity() == null ? 1 : item.getQuantity());
            items.add(i);
        }
        return createOrders(items, dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> orderCustom(PackageOrderDTO dto) {
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new BusinessException("请至少选择一件装备");
        }
        return createOrders(dto.getItems(), dto);
    }

    /**
     * 按装备清单逐件下单（同一事务，任一件库存不足则整体回滚）
     */
    private List<Long> createOrders(List<PackageOrderDTO.Item> items, PackageOrderDTO dto) {
        if (dto.getRentEndTime().isBefore(dto.getRentStartTime())) {
            throw new BusinessException("归还时间不能早于租赁开始时间");
        }
        List<Long> orderIds = new ArrayList<>();
        for (PackageOrderDTO.Item item : items) {
            OrderCreateDTO order = new OrderCreateDTO();
            order.setEquipmentId(item.getEquipmentId());
            order.setQuantity(item.getQuantity() == null ? 1 : item.getQuantity());
            order.setPickStoreId(dto.getStoreId());
            order.setReturnStoreId(dto.getReturnStoreId());
            order.setRentStartTime(dto.getRentStartTime());
            order.setRentEndTime(dto.getRentEndTime());
            orderIds.add(orderService.createOrder(order));
        }
        return orderIds;
    }
}
