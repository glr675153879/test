package com.hscloud.hs.cost.account.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hscloud.hs.cost.account.model.entity.CostAccountUnit;
import com.hscloud.hs.cost.account.model.vo.CostAccountUnitVo;
import com.pig4cloud.pigx.common.security.service.PigxUser;
import com.pig4cloud.pigx.common.security.util.SecurityUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
@Service
public class ISecondDistributionGetUnitIdService {

    public List<CostAccountUnitVo>  getUnitByUserId() {


        PigxUser user = SecurityUtils.getUser();
        //取出用户和部门所得人
//        List<CostUnitRelateInfo> dept = new CostUnitRelateInfo().selectList(new LambdaQueryWrapper<CostUnitRelateInfo>()
//                .select(CostUnitRelateInfo::getAccountUnitId,CostUnitRelateInfo::getName)
//                .eq(CostUnitRelateInfo::getType, "dept")
//                .in(CostUnitRelateInfo::getRelateId, user.getDeptIds())
//        );
//        List<CostUnitRelateInfo> userList = new CostUnitRelateInfo().selectList(new LambdaQueryWrapper<CostUnitRelateInfo>()
//                .select(CostUnitRelateInfo::getAccountUnitId,CostUnitRelateInfo::getName)
//                .eq(CostUnitRelateInfo::getType, "user")
//                .in(CostUnitRelateInfo::getRelateId, user.getId())
//        );
//        dept.addAll(userList);
        //取出当前登录人是科室负责人的科室列表
        List<CostAccountUnit> costAccountUnitList = new CostAccountUnit().selectList(new LambdaQueryWrapper<CostAccountUnit>()
                .eq(CostAccountUnit::getResponsiblePersonId, user.getId())
                .select(CostAccountUnit::getId,CostAccountUnit::getName,CostAccountUnit::getAccountGroupCode));
        List<CostAccountUnitVo> voList = costAccountUnitList.stream().map(costAccountUnit -> {
            CostAccountUnitVo unitVo = new CostAccountUnitVo();
            unitVo.setAccountUnitId(costAccountUnit.getId());
            unitVo.setAccountUnitName(costAccountUnit.getName());
            unitVo.setAccountGroupCode(costAccountUnit.getAccountGroupCode());
            return unitVo;

        }).collect(Collectors.toList());

        return voList;
//
//        Map<Long, String> map = dept.stream().collect(Collectors.toMap(CostUnitRelateInfo::getAccountUnitId
//                , CostUnitRelateInfo::getName, (existingValue, newValue) -> existingValue));// 处理key冲突的情况
//        List<CostUnitExcludedInfo> costUnitExcludedInfos = new CostUnitExcludedInfo().selectList(new LambdaQueryWrapper<CostUnitExcludedInfo>().eq(CostUnitExcludedInfo::getRelateId, user.getId()).select(
//                CostUnitExcludedInfo::getAccountUnitId
//        ));
//        //将两个map合并
//        unitMap.putAll(map);

//        List<Long> excludedIds = costUnitExcludedInfos.stream()
//                .map(CostUnitExcludedInfo::getAccountUnitId)
//                .collect(Collectors.toList());
//        excludedIds.forEach(map::remove);
    }
}
