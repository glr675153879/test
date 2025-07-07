package com.hscloud.hs.cost.account.service.impl.second;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hscloud.hs.cost.account.model.entity.second.GrantUnitLog;
import com.hscloud.hs.cost.account.model.entity.second.UnitTask;
import com.hscloud.hs.cost.account.service.second.IGrantUnitLogService;
import com.pig4cloud.pigx.common.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.second.GrantUnitMapper;
import com.hscloud.hs.cost.account.model.entity.second.GrantUnit;
import com.hscloud.hs.cost.account.service.second.IGrantUnitService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
* 发放单元 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GrantUnitService extends ServiceImpl<GrantUnitMapper, GrantUnit> implements IGrantUnitService {

    private final IGrantUnitLogService grantUnitLogService;

    @Override
    @Transactional
    public void init() {
        List<GrantUnit> list = this.list();
        for (GrantUnit grantUnit : list){
            grantUnit.setIfInit("1");
            //grantUnit.setStatus("1");
        }
        if(!list.isEmpty()){
            this.updateBatchById(list);
        }
    }

    @Override
    public Boolean ifInit() {
        GrantUnit grantUnit = this.getOne(Wrappers.<GrantUnit>lambdaQuery().last("limit 1"));
        return grantUnit != null && "1".equals(grantUnit.getIfInit());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLog(GrantUnit grantUnitDB, GrantUnit grantUnit) {
        Boolean ifInit = this.ifInit();
        if(!ifInit){
            return;
        }
        String content = "";
        if("1".equals(grantUnitDB.getStatus()) && "0".equals(grantUnit.getStatus())){
            GrantUnitLog grantUnitLog = new GrantUnitLog();
            content = "启用发放单元："+grantUnitDB.getName();
            grantUnitLog.setGrantUnitId(grantUnit.getId());
            grantUnitLog.setCreateBy(SecurityUtils.getUser().getName());
            grantUnitLog.setContent(content);
            grantUnitLogService.save(grantUnitLog);
            return;
        }else if("0".equals(grantUnitDB.getStatus()) && "1".equals(grantUnit.getStatus())){
            GrantUnitLog grantUnitLog = new GrantUnitLog();
            content = "停用发放单元："+grantUnitDB.getName();
            grantUnitLog.setGrantUnitId(grantUnit.getId());
            grantUnitLog.setCreateBy(SecurityUtils.getUser().getName());
            grantUnitLog.setContent(content);
            grantUnitLogService.save(grantUnitLog);
            return;
        }
        if(!Objects.equals(grantUnitDB.getName(),grantUnit.getName())){
            GrantUnitLog grantUnitLog = new GrantUnitLog();
            content = "修改发放单元名称：由 "+grantUnitDB.getName()+" 修改为 "+grantUnit.getName();
            grantUnitLog.setGrantUnitId(grantUnit.getId());
            grantUnitLog.setCreateBy(SecurityUtils.getUser().getName());
            grantUnitLog.setContent(content);
            grantUnitLogService.save(grantUnitLog);
        }
        if(!Objects.equals(grantUnitDB.getKsUnitNames(),grantUnit.getKsUnitNames())){
            GrantUnitLog grantUnitLog = new GrantUnitLog();
            content = "修改发放单元对应科室单元：由 "+grantUnitDB.getKsUnitNames()+" 修改为 "+grantUnit.getKsUnitNames();
            grantUnitLog.setGrantUnitId(grantUnit.getId());
            grantUnitLog.setCreateBy(SecurityUtils.getUser().getName());
            grantUnitLog.setContent(content);
            grantUnitLogService.save(grantUnitLog);
        }
        if(!Objects.equals(grantUnitDB.getLeaderNames(),grantUnit.getLeaderNames())){
            GrantUnitLog grantUnitLog = new GrantUnitLog();
            content = "修改发放单元负责人：由 "+grantUnitDB.getLeaderNames()+" 修改为 "+grantUnit.getLeaderNames();
            grantUnitLog.setGrantUnitId(grantUnit.getId());
            grantUnitLog.setCreateBy(SecurityUtils.getUser().getName());
            grantUnitLog.setContent(content);
            grantUnitLogService.save(grantUnitLog);
        }
    }

    public Set<String> managerUnits(Long currentUserId) {
        List<GrantUnit> list = super.list(Wrappers.<GrantUnit>lambdaQuery()
                .eq(GrantUnit::getStatus, "0").like(GrantUnit::getLeaderIds, "," + currentUserId + ","));
        Set<String> result = new HashSet<>();
        for (GrantUnit grantUnit : list) {
            String ksUnitIds = grantUnit.getKsUnitIds();
            String ksUnitIdsNonStaff = grantUnit.getKsUnitIdsNonStaff();
            if (StrUtil.isNotBlank(ksUnitIds)) {
                result.addAll(StrUtil.split(ksUnitIds, ","));
            }
            if (StrUtil.isNotBlank(ksUnitIdsNonStaff)) {
                result.addAll(StrUtil.split(ksUnitIdsNonStaff, ","));
            }
        }
        if (CollectionUtil.isEmpty(result)) {
            //防止空集合，查询时忽略这个条件导致全查
            result.add("-1");
        }
        return result;
    }

    @Override
    public String getDeptIds(Long grantUnitId) {
        GrantUnit grantUnit = super.getById(grantUnitId);
        if(grantUnit == null)
            return null;
        String ksUnitIds = grantUnit.getKsUnitIds();
        String ksUnitIdsNonStaff = grantUnit.getKsUnitIdsNonStaff();
        if(ksUnitIds == null && ksUnitIdsNonStaff == null)
            return null;
        String rtn = "-1";
        if(StrUtil.isNotBlank(ksUnitIds)){
            rtn += ","+ksUnitIds;
        }else if(StrUtil.isNotBlank(ksUnitIdsNonStaff)){
            rtn += ","+ksUnitIdsNonStaff;
        }
        return StrUtil.removePrefix(rtn, "-1,");
    }
}
