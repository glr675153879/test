package com.hscloud.hs.cost.account.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.CostAccountPlanCostConfigIndexMapper;
import com.hscloud.hs.cost.account.mapper.CostAccountPlanCostMapper;
import com.hscloud.hs.cost.account.model.dto.CostAccountPlanCostQueryDto;
import com.hscloud.hs.cost.account.model.dto.CostAccountPlanFormulaDto;
import com.hscloud.hs.cost.account.model.dto.FormulaDto;
import com.hscloud.hs.cost.account.model.entity.CostAccountPlanConfig;
import com.hscloud.hs.cost.account.model.entity.CostAccountPlanConfigFormula;
import com.hscloud.hs.cost.account.model.entity.CostAccountPlanCost;
import com.hscloud.hs.cost.account.model.entity.CostAccountPlanCostConfigIndex;
import com.hscloud.hs.cost.account.model.vo.ConfigList;
import com.hscloud.hs.cost.account.model.vo.PlanCostPreviewVo;
import com.hscloud.hs.cost.account.service.CostAccountPlanCostConfigIndexService;
import com.hscloud.hs.cost.account.service.CostAccountPlanCostService;
import com.hscloud.hs.cost.account.utils.ExpressionCheckHelper;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CostAccountPlanCostServiceImpl extends ServiceImpl<CostAccountPlanCostMapper, CostAccountPlanCost> implements CostAccountPlanCostService {
    @Lazy
    @Autowired
    private CostAccountPlanCostConfigIndexService costAccountPlanCostConfigIndexService;
    @Lazy
    @Autowired
    private CostAccountPlanCostConfigIndexMapper costAccountPlanCostConfigIndexMapper;

//    @Lazy
//    @Autowired
//    private CostAccountPlanConfigService costAccountPlanConfigService;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveCostFormula(CostAccountPlanFormulaDto dto){
        CostAccountPlanCost costAccountPlanCost =  BeanUtil.copyProperties(dto, CostAccountPlanCost.class);

        save(costAccountPlanCost);
    }

    @Override
    public List<CostAccountPlanCost> listAllCostFormula(Long planId) {
        LambdaQueryWrapper<CostAccountPlanCost> wrapper = new LambdaQueryWrapper<>();
        wrapper
                .eq(CostAccountPlanCost::getPlanId, planId);
        List<CostAccountPlanCost> cost=list(wrapper);
        return cost;
    }

    @Override
    public CostAccountPlanCost listCostFormula(Long id){
        LambdaQueryWrapper<CostAccountPlanCost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CostAccountPlanCost::getId, id);
        CostAccountPlanCost cost=getOne(wrapper);
        return cost;
    }

    @Override
    public int updateCostFormula(CostAccountPlanCostQueryDto dto){
        LambdaQueryWrapper<CostAccountPlanCost> wrapper = new LambdaQueryWrapper<>();
        wrapper
                .eq(CostAccountPlanCost::getPlanId,dto.getPlanId())
                .eq(CostAccountPlanCost::getAccountProportionObject, dto.getAccountProportionObject());
        //
        CostAccountPlanCost cost=getOne(wrapper);
        cost.setCarryRule(dto.getCarryRule());
        cost.setReservedDecimal(dto.getReservedDecimal());
        //当选择的公式已经设置过公式时
        if(cost.getAccountProportionObject()!=null&&!cost.getAccountProportionObject().equals(dto.getAccountProportionObject())){
            //costAccountPlanCostConfigIndexMapper.deleteByConfigId();
            LambdaQueryWrapper<CostAccountPlanConfig> configWrapper = new LambdaQueryWrapper<>();


            List<CostAccountPlanConfig> configs=costAccountPlanCostConfigIndexMapper.getCostAccountPlanConfigList(cost.getAccountProportionObject());
            //costAccountPlanConfigService.list(configWrapper);
            List<String> configObjects = new ArrayList<>();
            configs.forEach(config-> {
                String accountProportionObject = config.getAccountProportionObject();
                configObjects.add(accountProportionObject);
            });
            Set<String> uniqueConfigObjects = new HashSet<>(configObjects);
            configObjects.clear();
            configObjects.addAll(uniqueConfigObjects);
            configObjects.forEach(c->{
                costAccountPlanCostConfigIndexMapper.deleteByConfigKey(c);
            });


        }
        cost.setPlanCostFormula(dto.getFormulaDto().getExpression());
        //公式描述
        cost.setCostDescription(dto.getFormulaDto().getCostDescription());
        saveOrUpdate(cost);

        List<FormulaDto.FormulaParam> params=dto.getFormulaDto().getParams();
        params.forEach(param->{
            CostAccountPlanCostConfigIndex costConfigIndex=new CostAccountPlanCostConfigIndex();
            //确定是否传值
            costConfigIndex.setConfigId((param.getValue()));
            costConfigIndex.setName(param.getName());

            costConfigIndex.setConfigKey(param.getKey());
            costConfigIndex.setType(param.getType());
            costAccountPlanCostConfigIndexService.save(costConfigIndex);
        });
        return 0;
    }

//    public List<PlanCostPreviewVo> parsePlanCost(List<CostAccountPlanCost> costList){
//
//        ExpressionCheckHelper helper = new ExpressionCheckHelper();
//        List<PlanCostPreviewVo> previewVoList = costList.stream().map(cost -> {
//
//            Set<String> strings = helper.expressionQuantity(cost.getPlanCostFormula());
//            List<ConfigItemList> configItemLists = new ArrayList<>();
//            for (String configKey : strings) {
//                CostAccountPlanCostConfigIndex one = costAccountPlanCostConfigIndexService.getOne(Wrappers.<CostAccountPlanCostConfigIndex>lambdaQuery()
//                        .eq(CostAccountPlanCostConfigIndex::getConfigKey, configKey));
//                ConfigItemList configItemList = BeanUtil.copyProperties(one, ConfigItemList.class);
//                configItemLists.add(configItemList);
//            }
//            PlanCostPreviewVo previewVo = BeanUtil.copyProperties(cost, PlanCostPreviewVo.class);
//            previewVo.setConfigItemLists(configItemLists);
//            return previewVo;
//        }).collect(Collectors.toList());
//        return previewVoList;
//    }

    public List<PlanCostPreviewVo> parsePlanCost(List<CostAccountPlanConfigFormula> formulas){

        ExpressionCheckHelper helper = new ExpressionCheckHelper();
        List<PlanCostPreviewVo> previewVoList = formulas.stream().map(cost -> {

            Set<String> strings = helper.expressionQuantity(cost.getPlanCostFormula());
            List<ConfigList> configLists = new ArrayList<>();
            for (String configKey : strings) {
                CostAccountPlanCostConfigIndex one = costAccountPlanCostConfigIndexService.getOne(Wrappers.<CostAccountPlanCostConfigIndex>lambdaQuery()
                        .eq(CostAccountPlanCostConfigIndex::getConfigKey, configKey)
                        .eq(CostAccountPlanCostConfigIndex::getConfigId,cost.getId()));
                ConfigList configList = BeanUtil.copyProperties(one, ConfigList.class);
                configLists.add(configList);
            }
            PlanCostPreviewVo previewVo = BeanUtil.copyProperties(cost, PlanCostPreviewVo.class);
            previewVo.setConfigLists(configLists);
            return previewVo;
        }).collect(Collectors.toList());
        return previewVoList;
    }
}