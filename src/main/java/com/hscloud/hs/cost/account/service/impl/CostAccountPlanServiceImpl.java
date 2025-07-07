package com.hscloud.hs.cost.account.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.constant.enums.CostAccountProportionType;
import com.hscloud.hs.cost.account.mapper.*;
import com.hscloud.hs.cost.account.model.dto.CostAccountPlanQueryDto;
import com.hscloud.hs.cost.account.model.dto.CostAccountPlanStatusDto;
import com.hscloud.hs.cost.account.model.entity.*;
import com.hscloud.hs.cost.account.model.pojo.CostFormulaInfo;
import com.hscloud.hs.cost.account.model.vo.CostPlanAccountObjectVo;
import com.hscloud.hs.cost.account.service.CostAccountPlanConfigCustomUnitService;
import com.hscloud.hs.cost.account.service.CostAccountPlanConfigService;
import com.hscloud.hs.cost.account.service.CostAccountPlanService;
import com.hscloud.hs.cost.account.service.CostAccountUnitService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
@Service
public class CostAccountPlanServiceImpl extends ServiceImpl<CostAccountPlanMapper, CostAccountPlan> implements CostAccountPlanService {

    @Autowired
    private CostAccountPlanMapper costAccountPlanMapper;
    @Autowired
    private CostAccountPlanCostMapper costAccountPlanCostMapper;
    @Autowired
    private CostAccountPlanConfigService costAccountPlanConfigService;

    @Autowired
    private CostAccountPlanConfigCustomUnitService costAccountPlanConfigCustomUnitService;

    @Autowired
    private CostAccountPlanConfigIndexNewMapper costAccountPlanConfigIndexNewMapper;

    @Autowired
    private CostAccountUnitService costAccountUnitService;

    @Autowired
    private CostAccountPlanConfigMapper costAccountPlanConfigMapper;

    @Autowired
    private CostAccountPlanConfigCustomUnitMapper costAccountPlanConfigCustomUnitMapper;

    @Autowired
    private CostAccountPlanConfigFormulaMapper costAccountPlanConfigFormulaMapper;

    @Override
    public IPage<CostAccountPlanQueryDto> listPlan(CostAccountPlanQueryDto queryDto) {
        Page<CostAccountPlanQueryDto> planPage = new Page<>(queryDto.getCurrent(), queryDto.getSize());
        List<CostAccountPlan> plans = costAccountPlanMapper.listByQueryDto(planPage, queryDto).getRecords();
        List<CostAccountPlanQueryDto> queryDtos = new ArrayList<>();
        for (CostAccountPlan plan : plans) {
            CostAccountPlanQueryDto dto = BeanUtil.copyProperties(plan, CostAccountPlanQueryDto.class);
            //添加公式列表
            LambdaQueryWrapper<CostAccountPlanConfigFormula> wrapperCost = new LambdaQueryWrapper<>();
            wrapperCost.eq(CostAccountPlanConfigFormula::getPlanId, plan.getId());
            List<CostAccountPlanConfigFormula> cost = costAccountPlanConfigFormulaMapper.selectList(wrapperCost);
            dto.setListCostFormula(cost);
            //添加任务类型,任务分组,核算对象
            DistributionTaskGroup distributionTaskGroup=new DistributionTaskGroup().selectById(plan.getTaskGroupId());
            if (distributionTaskGroup!=null){
                dto.setTaskGroupName(distributionTaskGroup.getName());
                dto.setTaskGroupId(distributionTaskGroup.getId());
                dto.setTaskType(distributionTaskGroup.getType());
                dto.setAccountObject(distributionTaskGroup.getAccountObject());
            }
            queryDtos.add(dto);
        }
        planPage.setTotal(planPage.getTotal());
        planPage.setRecords(queryDtos);
        return planPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void switchStatus(CostAccountPlanStatusDto dto) {
        CostAccountPlan plan = costAccountPlanMapper.selectById(dto.getId());
        if (ObjectUtils.isEmpty(plan)) {
            throw new BizException("不存在记录");
        }
        plan.setStatus(dto.getStatus());
        updateById(plan);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void check(CostAccountPlanStatusDto dto) {
        CostAccountPlan plan = costAccountPlanMapper.selectById(dto.getId());
        if (ObjectUtils.isEmpty(plan)) {
            throw new BizException("不存在记录");
        }
        //plan.setCheckFlag(dto.getStatus());
        plan.setCheckFlag("1");
        updateById(plan);
    }

    @Override
    public CostAccountPlanQueryDto byId(Long id) {

        CostAccountPlan plan = getById(id);

        CostAccountPlanQueryDto dto = new CostAccountPlanQueryDto();
        dto = BeanUtil.copyProperties(plan, CostAccountPlanQueryDto.class);
        //添加公式列表

        if (null != dto) {
            LambdaQueryWrapper<CostAccountPlanConfigFormula> wrapperCost = new LambdaQueryWrapper<>();
            wrapperCost

                    .eq(CostAccountPlanConfigFormula::getPlanId, id);
            List<CostAccountPlanConfigFormula> cost = costAccountPlanConfigFormulaMapper.selectList(wrapperCost);
            dto.setListCostFormula(cost);
        }

        return dto;
    }


    @Override
    public List<CostPlanAccountObjectVo> listAccountObject(Long planId) {
        //根据planId查询所有的公式
        LambdaQueryWrapper<CostAccountPlanConfigFormula> formulaLambdaQueryWrapper = new LambdaQueryWrapper<>();
        formulaLambdaQueryWrapper.eq(CostAccountPlanConfigFormula::getPlanId, planId);
        List<CostAccountPlanConfigFormula> formulas = costAccountPlanConfigFormulaMapper.selectList(formulaLambdaQueryWrapper);
        Map<String, CostAccountPlanConfigFormula> objectMap = formulas.stream().collect(Collectors.toMap(CostAccountPlanConfigFormula::getAccountObject, Function.identity(), (key1, key2) -> key2));
        Map<Long, CostAccountPlanConfigFormula> unitMap = formulas.stream().collect(Collectors.toMap(CostAccountPlanConfigFormula::getCustomUnitId, Function.identity(), (key1, key2) -> key2));
        //根据planId分组查询所有的核算对象
        LambdaQueryWrapper<CostAccountPlanConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CostAccountPlanConfig::getPlanId, planId)
                .eq(CostAccountPlanConfig::getDelFlag, '0');
        wrapper.groupBy(CostAccountPlanConfig::getAccountProportionObject);
        wrapper.select(CostAccountPlanConfig::getAccountProportionObject);
        List<CostAccountPlanConfig> configs = costAccountPlanConfigMapper.selectList(wrapper);
        List<CostPlanAccountObjectVo> vos = new ArrayList<>();
        configs.forEach(config -> {
            if (!CostAccountProportionType.CUSTOMGROUP.getGroupArrange().equals(config.getAccountProportionObject())) {
                CostPlanAccountObjectVo vo = new CostPlanAccountObjectVo();
                vo.setAccountObjectId(config.getAccountProportionObject());
                vo.setAccountObjectName(CostAccountProportionType.getDescByGroupArrange(config.getAccountProportionObject()));
                CostAccountPlanConfigFormula costAccountPlanConfigFormula = objectMap.get(config.getAccountProportionObject());
                if (null != costAccountPlanConfigFormula) {
                    vo.setId(costAccountPlanConfigFormula.getId());
                    vo.setFormulaExpression(costAccountPlanConfigFormula.getPlanCostFormula());
                    vo.setCarryRule(costAccountPlanConfigFormula.getCarryRule());
                    vo.setReservedDecimal(costAccountPlanConfigFormula.getReservedDecimal());
                    List<CostFormulaInfo> costFormulaInfos = JSON.parseArray(costAccountPlanConfigFormula.getConfig(), CostFormulaInfo.class);
                    //把每个CostFormulaInfo的id值赋予value
                    costFormulaInfos.forEach(c -> {
                        c.setValue(c.getId());
                    });
                    vo.setFormulaInfoList(costFormulaInfos);
                }
                vos.add(vo);
            }
        });

        //根据planId查询所有的自定义单元，根据自定义单元的id分组查询所有的核算对象
        LambdaQueryWrapper<CostAccountPlanConfigCustomUnit> wrapperCustomUnit = new LambdaQueryWrapper<>();
        wrapperCustomUnit.eq(CostAccountPlanConfigCustomUnit::getPlanId, planId)
                .eq(CostAccountPlanConfigCustomUnit::getDel_flag, '0');
        wrapperCustomUnit.groupBy(CostAccountPlanConfigCustomUnit::getCustomUnitId);
        wrapperCustomUnit.select(CostAccountPlanConfigCustomUnit::getCustomUnitId);
        List<CostAccountPlanConfigCustomUnit> customUnits = costAccountPlanConfigCustomUnitMapper.selectList(wrapperCustomUnit);
        List<Long> unitIds = customUnits.stream().map(CostAccountPlanConfigCustomUnit::getCustomUnitId).collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(unitIds)) {
            costAccountUnitService.getBaseMapper().selectBatchIds(unitIds).forEach(unit -> {
                CostPlanAccountObjectVo vo = new CostPlanAccountObjectVo();
                vo.setAccountObjectId(CostAccountProportionType.CUSTOMGROUP.getGroupArrange());
                vo.setCustomUnitId(unit.getId());
                vo.setAccountObjectName(unit.getName());
                CostAccountPlanConfigFormula costAccountPlanConfigFormula = unitMap.get(unit.getId());
                if (null != costAccountPlanConfigFormula) {
                    vo.setId(costAccountPlanConfigFormula.getId());
                    vo.setFormulaExpression(costAccountPlanConfigFormula.getPlanCostFormula());
                    vo.setCarryRule(costAccountPlanConfigFormula.getCarryRule());
                    vo.setReservedDecimal(costAccountPlanConfigFormula.getReservedDecimal());
                    List<CostFormulaInfo> costFormulaInfos = JSON.parseArray(costAccountPlanConfigFormula.getConfig(), CostFormulaInfo.class);
                    costFormulaInfos.forEach(c -> c.setValue(c.getId()));
                    vo.setFormulaInfoList(costFormulaInfos);
                }

                vos.add(vo);
            });
        }
        return vos;
    }

    /**
     * 方案复制
     *
     * @param id
     */
    @Override
    @Transactional
    public void copyById(Long id) {
        //查询方案对象
        final CostAccountPlan accountPlan = getById(id);
        CostAccountPlan costAccountPlan = new CostAccountPlan();
        BeanUtil.copyProperties(accountPlan, costAccountPlan);
        costAccountPlan.setName(accountPlan.getName()+"2.0");
        costAccountPlan.setId(null);
        costAccountPlan.insert();
        //查询配置项
        final List<CostAccountPlanConfig> accountPlanConfigList = costAccountPlanConfigService.list(new LambdaQueryWrapper<CostAccountPlanConfig>()
                .eq(CostAccountPlanConfig::getPlanId, id));
        //更换planId
        accountPlanConfigList.stream().forEach(config -> {
            CostAccountPlanConfig costAccountPlanConfig = new CostAccountPlanConfig();
            BeanUtil.copyProperties(config, costAccountPlanConfig);
            costAccountPlanConfig.setPlanId(costAccountPlan.getId());
            costAccountPlanConfig.setId(null);
            costAccountPlanConfig.insert();
            copyCostAccountPlanConfigIndexNew(config.getId(), costAccountPlanConfig.getId());
        });
        //自定义配置表
        final List<CostAccountPlanConfigCustomUnit> accountPlanConfigCustomUnitList = costAccountPlanConfigCustomUnitService.list(new LambdaQueryWrapper<CostAccountPlanConfigCustomUnit>()
                .eq(CostAccountPlanConfigCustomUnit::getPlanId, id));
        //更换planId
        List<CostAccountPlanConfigCustomUnit> costAccountPlanConfigCustomUnits = accountPlanConfigCustomUnitList.stream()
                .map(unit -> {
                    unit.setId(null);
                    unit.setPlanId(costAccountPlan.getId());
                    return unit;
                }).collect(Collectors.toList());
        costAccountPlanConfigCustomUnitService.saveBatch(costAccountPlanConfigCustomUnits);
        //公式
        final List<CostAccountPlanConfigFormula> accountPlanConfigFormulaList = costAccountPlanConfigFormulaMapper.selectList(new LambdaQueryWrapper<CostAccountPlanConfigFormula>()
                .eq(CostAccountPlanConfigFormula::getPlanId, id));
        accountPlanConfigFormulaList.stream().forEach(planConfigFormula -> {
            CostAccountPlanConfigFormula costAccountPlanConfigFormula =new CostAccountPlanConfigFormula();
            BeanUtil.copyProperties(planConfigFormula,costAccountPlanConfigFormula);
            costAccountPlanConfigFormula.setId(null);
            costAccountPlanConfigFormula.setPlanId(costAccountPlan.getId());
            costAccountPlanConfigFormula.insert();
        });
    }

    /**
     * 此方法用于复制CostAccountPlanConfigIndexNew
     *
     * @param oldId
     * @param newId
     */
    private void copyCostAccountPlanConfigIndexNew(Long oldId, Long newId) {
        //配置项
        final List<CostAccountPlanConfigIndexNew> planConfigIndexNewList = costAccountPlanConfigIndexNewMapper.selectList(new LambdaQueryWrapper<CostAccountPlanConfigIndexNew>()
                .eq(CostAccountPlanConfigIndexNew::getPlanConfigId, oldId));
        planConfigIndexNewList.stream().forEach(planConfigIndexNew -> {
            CostAccountPlanConfigIndexNew costAccountPlanConfigIndexNew = new CostAccountPlanConfigIndexNew();
            BeanUtil.copyProperties(planConfigIndexNew, costAccountPlanConfigIndexNew);
            costAccountPlanConfigIndexNew.setId(null);
            costAccountPlanConfigIndexNew.setPlanConfigId(newId);
            costAccountPlanConfigIndexNew.insert();
        });

    }

}
