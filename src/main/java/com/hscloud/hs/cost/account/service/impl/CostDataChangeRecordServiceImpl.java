package com.hscloud.hs.cost.account.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.CostDataChangeRecordMapper;
import com.hscloud.hs.cost.account.model.dto.CostDataChangeRecordCountDto;
import com.hscloud.hs.cost.account.model.entity.CostDataChangeRecord;
import com.hscloud.hs.cost.account.model.vo.CostDataChangeRecordCountVo;
import com.hscloud.hs.cost.account.model.vo.CostDataChangeRecordVo;
import com.hscloud.hs.cost.account.service.ICostDataChangeRecordService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 数据变更记录 服务实现类
 * </p>
 *
 * @author 
 * @since 2023-09-11
 */
@Service
public class CostDataChangeRecordServiceImpl extends ServiceImpl<CostDataChangeRecordMapper, CostDataChangeRecord> implements ICostDataChangeRecordService {

    @Override
    public List<CostDataChangeRecordVo> getChangeRecord(String bizCode) {
        //TODO 查询异动列表
        LambdaQueryWrapper<CostDataChangeRecord> queryWrapper = new LambdaQueryWrapper<CostDataChangeRecord>();
        queryWrapper.eq(CostDataChangeRecord::getBizCode, bizCode)
                .eq(CostDataChangeRecord::getStatus,"0");
        List<CostDataChangeRecordVo> costDataChangeRecordVos = new ArrayList<>();
        for (CostDataChangeRecord costDataChangeRecord : baseMapper.selectList(queryWrapper)) {
            CostDataChangeRecordVo costDataChangeRecordVo = BeanUtil.copyProperties(costDataChangeRecord, CostDataChangeRecordVo.class);
            costDataChangeRecordVos.add(costDataChangeRecordVo);
        }
        return costDataChangeRecordVos;
    }

    @Override
    public CostDataChangeRecordCountVo getCount(CostDataChangeRecordCountDto dto) {
        CostDataChangeRecordCountVo countVo = new CostDataChangeRecordCountVo();
        List<CostDataChangeRecordCountVo.BizObject> bizObjectList = new ArrayList<>();
        Long changeRecordCount = 0L;
        for (Long bizId : dto.getBizIds()) {
            CostDataChangeRecordCountVo.BizObject bizObject = new CostDataChangeRecordCountVo.BizObject();
            LambdaQueryWrapper<CostDataChangeRecord> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CostDataChangeRecord::getBizCode, dto.getBizCode())
                    .eq(CostDataChangeRecord::getBizId, bizId)
                    .eq(CostDataChangeRecord::getStatus,"0");
            long count = count(queryWrapper);
            changeRecordCount += count;
            bizObject.setBizId(bizId);
            bizObject.setBizCount(count);
            bizObjectList.add(bizObject);
        }
        countVo.setChangeRecordCount(changeRecordCount);
        countVo.setBizObjectList(bizObjectList);
        return countVo;
    }
}
