package com.hscloud.hs.cost.account.service.impl.dataReport;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.constant.enums.kpi.MemberEnum;
import com.hscloud.hs.cost.account.mapper.dataReport.CostClusterUnitMapper;
import com.hscloud.hs.cost.account.mapper.kpi.KpiMemberMapper;
import com.hscloud.hs.cost.account.model.entity.CostAccountUnit;
import com.hscloud.hs.cost.account.model.entity.dataReport.CostClusterUnit;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountUnit;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiMember;
import com.hscloud.hs.cost.account.service.dataReport.ICostClusterUnitService;
import com.hscloud.hs.cost.account.service.impl.kpi.KpiAccountUnitService;
import com.hscloud.hs.cost.account.utils.OperationUtil;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 归集单元 服务实现类
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CostClusterUnitService extends ServiceImpl<CostClusterUnitMapper, CostClusterUnit> implements ICostClusterUnitService {

    // private final RemoteThirdAccountUnitService remoteThirdAccountUnitService;
    private final KpiMemberMapper kpiMemberMapper;
    private final OperationUtil operationUtil;
    private final KpiAccountUnitService kpiAccountUnitService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean activate(CostClusterUnit costClusterUnit) {
        CostClusterUnit newCostClusterUnit = getById(costClusterUnit.getId());
        if ("0".equals(newCostClusterUnit.getStatus())) {
            newCostClusterUnit.setStatus("1");
            updateById(newCostClusterUnit);
        } else if ("1".equals(newCostClusterUnit.getStatus())) {
            newCostClusterUnit.setStatus("0");
            updateById(newCostClusterUnit);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean initiate() {
        List<CostClusterUnit> costClusterUnitList = list();
        for (CostClusterUnit costClusterUnit : costClusterUnitList) {
            costClusterUnit.setInitialized("Y");
        }
        return updateBatchById(costClusterUnitList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveData(CostClusterUnit costClusterUnit) {
        processUnitList(costClusterUnit);

        save(costClusterUnit);

        if ("0".equals(costClusterUnit.getIsFixUnit())) {
            KpiMember member = new KpiMember();
            member.setHostId(costClusterUnit.getId());
            member.setMemberId(0L);
            member.setPeriod(0L);
            member.setMemberType(MemberEnum.CLUSTER_DEPT.getType());
            kpiMemberMapper.insert(member);
        } else if (costClusterUnit.getUnitList() != null && !costClusterUnit.getUnitList().isEmpty()) {
            costClusterUnit.getUnitList().forEach(t -> {
                KpiMember member = new KpiMember();
                member.setHostId(costClusterUnit.getId());
                member.setMemberId(t.getId());
                member.setPeriod(0L);
                member.setMemberType(MemberEnum.CLUSTER_DEPT.getType());
                kpiMemberMapper.insert(member);
            });
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean editData(CostClusterUnit costClusterUnit) {
        processUnitList(costClusterUnit);

        kpiMemberMapper.delete(new LambdaQueryWrapper<KpiMember>()
                .eq(KpiMember::getMemberType, MemberEnum.CLUSTER_DEPT.getType())
                .eq(KpiMember::getHostId, costClusterUnit.getId()));
        if ("0".equals(costClusterUnit.getIsFixUnit())) {
            KpiMember member = new KpiMember();
            member.setHostId(costClusterUnit.getId());
            member.setMemberId(0L);
            member.setPeriod(0L);
            member.setMemberType(MemberEnum.CLUSTER_DEPT.getType());
            kpiMemberMapper.insert(member);
        } else if (costClusterUnit.getUnitList() != null && !costClusterUnit.getUnitList().isEmpty()) {
            costClusterUnit.getUnitList().forEach(t -> {
                KpiMember member = new KpiMember();
                member.setHostId(costClusterUnit.getId());
                member.setMemberId(t.getId());
                member.setPeriod(0L);
                member.setMemberType(MemberEnum.CLUSTER_DEPT.getType());
                kpiMemberMapper.insert(member);
            });
        }
        return updateById(costClusterUnit);
    }

    // @Override
    // @Transactional(rollbackFor = Exception.class)
    // public Boolean setClusterUnit(Long unitId) {
    //     ThirdAccountUnit unitVo = remoteThirdAccountUnitService.getById(unitId).getData();
    //     //0:否 1:是，不是归集单元则删除归集
    //     if (unitVo.getIsClusterUnit().equals("1")) {
    //         //之前已经设置了归集单元，那么需要找到这个归集单元，判断状态
    //         LambdaQueryWrapper<CostClusterUnit> qr = new LambdaQueryWrapper<>();
    //         qr.eq(CostClusterUnit::getThirdAccountId, unitVo.getId());
    //         CostClusterUnit costClusterUnit = getOne(qr, false);
    //         if (Objects.nonNull(costClusterUnit)) {
    //             //只能删除 未初始化或者停用状态的归集单元
    //             if (!(Objects.equals(costClusterUnit.getInitialized(), "N") || Objects.equals(costClusterUnit.getStatus(), "1"))) {
    //                 throw new BizException("只能删除未初始化或者停用状态的归集单元");
    //             }
    //             removeById(costClusterUnit);
    //         }
    //     } else {
    //         ThirdAccountUnit unit = new ThirdAccountUnit();
    //         BeanUtils.copyProperties(unitVo, unit);
    //         CostClusterUnit costClusterUnit = new CostClusterUnit();
    //         costClusterUnit.setName(unitVo.getThirdUnitName());
    //         costClusterUnit.setThirdAccountId(unitVo.getId());
    //         costClusterUnit.setIsFixUnit("1");
    //         costClusterUnit.setThirdId(unitVo.getThirdId());
    //         costClusterUnit.setThirdName(unitVo.getThirdName());
    //         save(costClusterUnit);
    //     }
    //     remoteThirdAccountUnitService.setClusterUnit(unitId);
    //     return true;
    // }

    @Transactional(rollbackFor = Exception.class)
    public void processUnitList(CostClusterUnit costClusterUnit) {
        StringBuilder sb = new StringBuilder();
        List<CostAccountUnit> unitList = costClusterUnit.getUnitList();
        if (unitList == null || unitList.isEmpty()) {
            costClusterUnit.setUnits("[]");
            return;
        }
        sb.append("[");
        for (CostAccountUnit costAccountUnit : unitList) {
            sb.append("{\"id\": \"").append(costAccountUnit.getId()).append("\", \"name\": \"").append(costAccountUnit.getName()).append("\"},");
        }
        sb.deleteCharAt(sb.length() - 1);//移除最后一个逗号
        sb.append("]");
        costClusterUnit.setUnits(sb.toString());
    }

    /**
     * 根据实体(ID)删除
     * 删除前联动将第三方应用归集单元状态设置为否
     *
     * @param id
     * @since 3.4.4
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean del(Long id) {
        CostClusterUnit byId = super.getById(id);
        if (Objects.isNull(byId)) {
            throw new BizException("该数据不存在");
        }
        // if (Objects.nonNull(byId.getThirdAccountId())) {
        //     ThirdAccountUnit unitVo = remoteThirdAccountUnitService.getById(byId.getThirdAccountId()).getData();
        //     if (Objects.nonNull(unitVo) && Objects.equals(unitVo.getIsClusterUnit(), "1")) {
        //         remoteThirdAccountUnitService.setClusterUnit(byId.getThirdAccountId());
        //     }
        // }
        kpiMemberMapper.delete(new LambdaQueryWrapper<KpiMember>()
                .eq(KpiMember::getMemberType, MemberEnum.CLUSTER_DEPT.getType())
                .eq(KpiMember::getHostId, id));
        return super.removeById(byId);
    }

    @Override
    public IPage<CostClusterUnit> pageClusterUnit(Page<CostClusterUnit> page, QueryWrapper<CostClusterUnit> wrapper) {
        Page<CostClusterUnit> clusterUnitPage = page(page, wrapper.orderByDesc("create_time").eq("tenant_id", SecurityUtils.getUser().getTenantId()));
        List<CostClusterUnit> records = clusterUnitPage.getRecords();

        List<KpiAccountUnit> list = kpiAccountUnitService.list();
        for (CostClusterUnit record : records) {
            if (StringUtils.isNotBlank(record.getUnits())) {
                List<JSONObject> list1 = JSONUtil.toList(record.getUnits(), JSONObject.class);
                for (JSONObject jsonObject : list1) {
                    KpiAccountUnit accountUnit = list.stream().filter(o -> o.getId().toString().equals(jsonObject.get("id"))).findFirst().orElse(null);
                    jsonObject.set("name", accountUnit == null ? jsonObject.get("name") + "(已删除或禁用)" : accountUnit.getName());
                }
                record.setUnits(JSONUtil.toJsonStr(list1));
            }
        }

        Map<Long, String> userMap = operationUtil.getUserMap(records);
        records.forEach(item -> {
            //操作人、操作时间
            item.setOperationName(operationUtil.getOperationName(userMap, item));
            item.setOperationTime(operationUtil.getOperationTime(item));
        });
        return clusterUnitPage;
    }
}
