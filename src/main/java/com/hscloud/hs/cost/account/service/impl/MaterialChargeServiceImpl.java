package com.hscloud.hs.cost.account.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.constant.ResourceIsChargeEnum;
import com.hscloud.hs.cost.account.constant.enums.EnableEnum;
import com.hscloud.hs.cost.account.mapper.MaterialChargeMapper;
import com.hscloud.hs.cost.account.model.dto.MaterialChargeQueryDto;
import com.hscloud.hs.cost.account.model.dto.MaterialChargeStatusDto;
import com.hscloud.hs.cost.account.model.entity.MaterialCharge;
import com.hscloud.hs.cost.account.model.vo.MaterialChargeVo;
import com.hscloud.hs.cost.account.service.IMaterialChargeService;
import com.hscloud.hs.cost.account.utils.DmoUtil;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * 物资管理
 *
 * @author lian
 * @date 2024/6/2 15:09
 */

@Service
@RequiredArgsConstructor
public class MaterialChargeServiceImpl extends ServiceImpl<MaterialChargeMapper, MaterialCharge> implements IMaterialChargeService {

    private final MaterialChargeMapper materialChargeMapper;

    private final DmoUtil dmoUtil;

    @Override
    public IPage<MaterialCharge> pageList(MaterialChargeQueryDto dto) {
        LambdaQueryWrapper<MaterialCharge> queryWrapper = getLambdaQueryWrapper(dto);
        return page(new Page<>(dto.getCurrent(), dto.getSize()), queryWrapper);
    }

    @NotNull
    private static LambdaQueryWrapper<MaterialCharge> getLambdaQueryWrapper(MaterialChargeQueryDto dto) {
        LambdaQueryWrapper<MaterialCharge> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.like(Objects.nonNull(dto.getStoreName()), MaterialCharge::getStoreName, dto.getStoreName());
        queryWrapper.like(Objects.nonNull(dto.getResourceName()), MaterialCharge::getResourceName, dto.getResourceName());
        queryWrapper.eq(Objects.nonNull(dto.getIsCharge()), MaterialCharge::getIsCharge, dto.getIsCharge());
        queryWrapper.eq(Objects.nonNull(dto.getStatus()), MaterialCharge::getStatus, dto.getStatus());
        if (StrUtil.isNotBlank(dto.getMatchType())) {
            if (ObjectUtil.equal("1", dto.getMatchType())) {
                queryWrapper.isNotNull(MaterialCharge::getIsCharge);
            } else if (ObjectUtil.equal("2", dto.getMatchType())) {
                queryWrapper.isNull(MaterialCharge::getIsCharge);
            }
        }
        queryWrapper.orderByDesc(MaterialCharge::getUpdateTime).orderByDesc(MaterialCharge::getId);
        return queryWrapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @XxlJob("syncMaterialChargeData")
    public Object syncData(MaterialChargeQueryDto dto) {
        int pageSize = 200;
        long current = Objects.nonNull(dto) ?dto.getCurrent():1;
        HashSet<String> hashSet = getRecords(new HashSet<>());
        while (true) {
            List<MaterialChargeVo> materialChargeVos = dmoUtil.queryMaterialChargeList(current, pageSize);
            if (CollectionUtils.isEmpty(materialChargeVos)) {
                break; // 如果查询结果为空，提前结束循环
            }
            List<MaterialCharge> materialCharges = new ArrayList<>();
            materialChargeVos.forEach(materialChargeVo -> {
                if(!hashSet.contains(materialChargeVo.getStoreId()+"-"+materialChargeVo.getResourceId())){
                    MaterialCharge m = new MaterialCharge();
                    m.setResourceId(materialChargeVo.getResourceId());
                    m.setResourceName(materialChargeVo.getResourceName());
                    m.setStoreId(materialChargeVo.getStoreId());
                    m.setStoreName(materialChargeVo.getStoreName());
                    m.setStatus(EnableEnum.ENABLE.getType());
                    //获取数据小组数据时,可收费和不可收费对应状态设置
                    if(StrUtil.isNotBlank(materialChargeVo.getIsCharge())){
                        ResourceIsChargeEnum byCode = ResourceIsChargeEnum.getByDesc(materialChargeVo.getIsCharge());
                        if(!byCode.equals(ResourceIsChargeEnum.NOT_SET)
                            && !byCode.equals(ResourceIsChargeEnum.DEFAULT)){
                            m.setIsCharge(byCode.getType());
                        }
                    }
                    materialCharges.add(m);
                }
            });
            this.saveBatch(materialCharges); // 批量保存
            if (materialChargeVos.size() < pageSize) {
                break; // 如果查询结果少于 pageSize，说明已经到最后一页
            }
            current += 1; // 获取下一页
        }
        return null;
    }

    public HashSet<String> getRecords(HashSet<String> hashSet){
        List<MaterialCharge> materialCharges = this.baseMapper.selectList(Wrappers.lambdaQuery(MaterialCharge.class));
        if(!CollectionUtils.isEmpty(materialCharges)){
            materialCharges.forEach(materialCharge -> hashSet.add(materialCharge.getStoreId()+"-"+materialCharge.getResourceId()));
        }
        return hashSet;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void switchStatus(MaterialChargeStatusDto dto) {
        MaterialCharge materialCharge = this.baseMapper.selectById(dto.getId());
        if(Objects.nonNull(materialCharge)){
            materialCharge.setStatus(dto.getStatus());
            this.updateById(materialCharge);
        }
    }
}
