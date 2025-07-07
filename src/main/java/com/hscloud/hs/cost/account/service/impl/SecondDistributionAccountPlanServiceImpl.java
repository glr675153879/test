package com.hscloud.hs.cost.account.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.Joiner;
import com.hscloud.hs.cost.account.constant.enums.*;
import com.hscloud.hs.cost.account.mapper.SecondDistributionAccountPlanMapper;
import com.hscloud.hs.cost.account.model.dto.*;
import com.hscloud.hs.cost.account.model.entity.*;
import com.hscloud.hs.cost.account.model.pojo.SecondDistributionFormula;
import com.hscloud.hs.cost.account.model.vo.*;
import com.hscloud.hs.cost.account.service.*;
import com.pig4cloud.pigx.common.core.exception.BizException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 二次分配方案表 服务实现类
 * </p>
 *
 * @author 
 * @since 2023-11-17
 */
@Service
public class SecondDistributionAccountPlanServiceImpl extends ServiceImpl<SecondDistributionAccountPlanMapper, SecondDistributionAccountPlan> implements ISecondDistributionAccountPlanService {

    @Autowired
    private ISecondDistributionAccountIndexService secondDistributionAccountIndexService;

    @Autowired
    private ISecondDistributionAccountFormulaParamService secondDistributionAccountFormulaParamService;

    @Autowired
    private ISecondDistributionPlanConfigFormulaService secondDistributionPlanConfigFormulaService;

    @Autowired
    private ISecondDistributionSettingsManagementService secondDistributionSettingsManagementService;

    @Override
    public SecondDistributionGetAccountPlanDetailsVo getDistributionAccountPlanDetails(Long planId,Long unitId) {
        //返回值
        SecondDistributionGetAccountPlanDetailsVo res = new SecondDistributionGetAccountPlanDetailsVo();
        SecondDistributionAccountPlan accountPlan;
        if (planId != null) {
            //获取方案配置表信息
            accountPlan= baseMapper.selectById(planId);
            if(accountPlan == null){
                return res;
            }
            BeanUtils.copyProperties(accountPlan, res);
        }else {
            //获取最新的方案配置表信息
            accountPlan= baseMapper.getAccountPlanLasted(unitId);
            if(accountPlan == null){
                return res;
            }
        }
        //出参设置方案配置id
        res.setPlanId(accountPlan.getId());

        //封装核算指标配置
        List<Long> accountIndexIds = new ArrayList<>();
        if(StringUtils.isNotBlank(accountPlan.getAccountIndexIds())){
            accountIndexIds = Arrays.stream(accountPlan.getAccountIndexIds().split(","))
                    .map(Long::valueOf).collect(Collectors.toList());
        }

        accountIndexIds.stream().forEach(r ->{
            SecondDistributionAccountIndexInfoVo secondDistributionAccountIndexInfoVo =
                    new SecondDistributionAccountIndexInfoVo();
            //根据方案配置表id 和 核算指标id  获取所有的核算指标
            SecondDistributionAccountIndex secondDistributionAccountIndex =
                    secondDistributionAccountIndexService.getOne(new LambdaQueryWrapper<SecondDistributionAccountIndex>()
                            .eq(SecondDistributionAccountIndex::getId, r)
                            .eq(SecondDistributionAccountIndex::getPlanId, accountPlan.getId()));
            if(secondDistributionAccountIndex == null){
                log.error("当前方案配置获取核算指标信息不存在");
                throw new BizException("当前方案配置获取核算指标信息不存在");
            }

            BeanUtils.copyProperties(secondDistributionAccountIndex, secondDistributionAccountIndexInfoVo);
            secondDistributionAccountIndexInfoVo.setId(r);

            if(SecondDistributionIndexEnum.KNGLJX.getItem().equals(secondDistributionAccountIndex.getAccountIndex())){
                //设置标识
                secondDistributionAccountIndexInfoVo.setTag(SecondDistributionIndexEnum.KNGLJX.getTag());

                //管理
                List<Long> managementIds = new ArrayList<>();
                if(StringUtils.isNotBlank(secondDistributionAccountIndex.getBizContent())){
                    managementIds = Arrays.stream(secondDistributionAccountIndex.getBizContent().split(","))
                            .map(Long::valueOf).collect(Collectors.toList());
                }
                managementIds.stream().forEach(rr ->{
                    ManagementItemVo managementItemVo = new ManagementItemVo();
                    SecondDistributionSettingsManagement secondDistributionSettingsManagement =
                            secondDistributionSettingsManagementService.getById(rr);
                    managementItemVo.setId(secondDistributionSettingsManagement.getId());
                    managementItemVo.setAmount(secondDistributionSettingsManagement.getAmount());
                    managementItemVo.setPosition(secondDistributionSettingsManagement.getPosition());
                    managementItemVo.setUnit(secondDistributionSettingsManagement.getUnit());
                    secondDistributionAccountIndexInfoVo.getManagementInfos().add(managementItemVo);
                });

            }else if(SecondDistributionIndexEnum.DXJX.getItem().equals(secondDistributionAccountIndex.getAccountIndex())||
                    SecondDistributionIndexEnum.GRZCXS.getItem().equals(secondDistributionAccountIndex.getAccountIndex())||
                    SecondDistributionIndexEnum.GZLJX.getItem().equals(secondDistributionAccountIndex.getAccountIndex())||
                    SecondDistributionIndexEnum.PJFPJX.getItem().equals(secondDistributionAccountIndex.getAccountIndex())){
                //设置标识(因为都一样，统一使用DXJX的tag，不额外进行区分)
                secondDistributionAccountIndexInfoVo.setTag(SecondDistributionIndexEnum.DXJX.getTag());

                //公式
                SecondDistributionFormula secondDistributionFormula = new SecondDistributionFormula();
                secondDistributionFormula.setExpression(secondDistributionAccountIndex.getBizContent());

                //查询相关公式参数
                List<SecondDistributionAccountFormulaParam> secondDistributionAccountFormulaParamList =
                        secondDistributionAccountFormulaParamService
                                .list(new LambdaQueryWrapper<SecondDistributionAccountFormulaParam>()
                                        .eq(SecondDistributionAccountFormulaParam::getBizId,
                                                secondDistributionAccountIndex.getId())
                                        .eq(SecondDistributionAccountFormulaParam::getPlanId,
                                                accountPlan.getId())
                                        .eq(SecondDistributionAccountFormulaParam::getType,
                                                SecondDistributionFormulaParamTypeEnum.HSZB.getType()));

                secondDistributionAccountFormulaParamList.stream().forEach(rr -> {
                    SecondDistributionFormula.FormulaParam formulaParam = new SecondDistributionFormula.FormulaParam();
                    formulaParam.setKey(rr.getFormulaKey());
                    formulaParam.setName(rr.getFormulaName());
                    formulaParam.setType(rr.getFormulaType());
                    formulaParam.setValue(rr.getFormulaValue());
                    secondDistributionFormula.getParams().add(formulaParam);
                });

                secondDistributionAccountIndexInfoVo.setOtherFormula(secondDistributionFormula);
            }
            res.getAccountIndexInfoList().add(secondDistributionAccountIndexInfoVo);
        });


        //封装总公式
        List<Long> formulaIds = new ArrayList<>();
        if(StringUtils.isNotBlank(accountPlan.getFormulaIds())){
            formulaIds = Arrays.stream(accountPlan.getFormulaIds().split(","))
                    .map(Long::valueOf).collect(Collectors.toList());
        }

        formulaIds.stream().forEach(r -> {
            //查询总公式信息
            SecondDistributionPlanConfigFormula secondDistributionPlanConfigFormula =
                    secondDistributionPlanConfigFormulaService.getOne(
                            new LambdaQueryWrapper<SecondDistributionPlanConfigFormula>()
                            .eq(SecondDistributionPlanConfigFormula::getId, r)
                            .eq(SecondDistributionPlanConfigFormula::getPlanId,
                                    accountPlan.getId()));
            if(secondDistributionPlanConfigFormula == null){
                log.error("当前方案配置获取总公式信息不存在");
                throw new BizException("当前方案配置获取总公式信息不存在");
            }

            SecondDistributionPlanConfigFormulaVo secondDistributionPlanConfigFormulaVo =
                    new SecondDistributionPlanConfigFormulaVo();

            BeanUtils.copyProperties(secondDistributionPlanConfigFormula, secondDistributionPlanConfigFormulaVo);
            secondDistributionPlanConfigFormulaVo.getOtherFormula()
                    .setExpression(secondDistributionPlanConfigFormula.getPlanCostFormula());

            //公式参数
            List<SecondDistributionAccountFormulaParam> secondDistributionAccountFormulaParamList =
                    secondDistributionAccountFormulaParamService
                            .list(new LambdaQueryWrapper<SecondDistributionAccountFormulaParam>()
                                    .eq(SecondDistributionAccountFormulaParam::getBizId,
                                            secondDistributionPlanConfigFormula.getId())
                                    .eq(SecondDistributionAccountFormulaParam::getPlanId,
                                            accountPlan.getId())
                                    .eq(SecondDistributionAccountFormulaParam::getType,
                                            SecondDistributionFormulaParamTypeEnum.ZGS.getType()));

            secondDistributionAccountFormulaParamList.stream().forEach(rr ->{
                SecondDistributionFormula.FormulaParam formulaParam = new SecondDistributionFormula.FormulaParam();
                formulaParam.setKey(rr.getFormulaKey());
                formulaParam.setName(rr.getFormulaName());
                formulaParam.setType(rr.getFormulaType());
                formulaParam.setValue(rr.getFormulaValue());
                secondDistributionPlanConfigFormulaVo.getOtherFormula().getParams().add(formulaParam);
            });
            res.setConfigFormula(secondDistributionPlanConfigFormulaVo);
        });
        return res;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveDistributionAccountPlan(saveDistributionAccountPlanDto input) {
        //获取新的方案配置表
        SecondDistributionAccountPlan secondDistributionAccountPlan = new SecondDistributionAccountPlan();
        //设置科室单元id（不为空）
        secondDistributionAccountPlan.setUnitId(input.getUnitId());
        //get方案分配表的 id
        secondDistributionAccountPlan.insert();

        //二次分配核算指标表
        Integer seq = 1;    //记录顺序
        //获取核算配置指标信息
        List<SecondDistributionAccountIndexInfoDto> accountIndexInfoDtoList = input.getAccountIndexInfoList();

        List<Long> accountIndexIds = new ArrayList<>();
        for (SecondDistributionAccountIndexInfoDto accountIndexInfo : accountIndexInfoDtoList) {
            //二次分配方案核算指标表实体类对象
            SecondDistributionAccountIndex secondDistributionAccountIndex = new SecondDistributionAccountIndex();
            BeanUtils.copyProperties(accountIndexInfo, secondDistributionAccountIndex);
            secondDistributionAccountIndex.setUnitId(input.getUnitId());

            if(SecondDistributionIndexEnum.KNGLJX.getItem().equals(accountIndexInfo.getAccountIndex())){
                //管理绩效核算指标

                secondDistributionAccountIndex.setPlanId(secondDistributionAccountPlan.getId());
                secondDistributionAccountIndex.setSeq(seq ++);
                if(accountIndexInfo.getManagementInfos() != null){
                    List<Long> managementIds = new ArrayList<>();
                    List<ManagementItemVo> managementInfos = accountIndexInfo.getManagementInfos();
                    managementInfos.stream().forEach(r -> {
                        managementIds.add(r.getId());
                    });
                    String bizContent = Joiner.on(",").join(managementIds);
                    secondDistributionAccountIndex.setBizContent(bizContent);
                }
                //核算指标落库
                secondDistributionAccountIndex.insert();
            }else if(SecondDistributionIndexEnum.DXJX.getItem().equals(accountIndexInfo.getAccountIndex())||
                    SecondDistributionIndexEnum.GRZCXS.getItem().equals(accountIndexInfo.getAccountIndex())||
                    SecondDistributionIndexEnum.GZLJX.getItem().equals(accountIndexInfo.getAccountIndex())||
                    SecondDistributionIndexEnum.PJFPJX.getItem().equals(accountIndexInfo.getAccountIndex())){
                //公式核算指标

                secondDistributionAccountIndex.setPlanId(secondDistributionAccountPlan.getId());
                //公式
                secondDistributionAccountIndex.setBizContent(accountIndexInfo.getOtherFormula().getExpression());
                //设置当前新增核算指标序号
                secondDistributionAccountIndex.setSeq(seq ++);
                //核算指标落库
                secondDistributionAccountIndex.insert();
                //存储公式参数项
                accountIndexInfo.getOtherFormula().getParams().stream().forEach(r -> {
                    SecondDistributionAccountFormulaParam secondDistributionAccountFormulaParam =
                            new SecondDistributionAccountFormulaParam();
                    secondDistributionAccountFormulaParam.setType(SecondDistributionFormulaParamTypeEnum.HSZB.getType());
                    secondDistributionAccountFormulaParam.setFormulaKey(r.getKey());
                    secondDistributionAccountFormulaParam.setFormulaName(r.getName());
                    secondDistributionAccountFormulaParam.setFormulaType(r.getType());
                    secondDistributionAccountFormulaParam.setFormulaValue(r.getValue());
                    secondDistributionAccountFormulaParam.setUnitId(input.getUnitId());
                    secondDistributionAccountFormulaParam.setPlanId(secondDistributionAccountPlan.getId());
                    secondDistributionAccountFormulaParam.setBizId(secondDistributionAccountIndex.getId());
                    secondDistributionAccountFormulaParam.insert();
                });
            }

            //将核算指标添加到方案配置表中
            accountIndexIds.add(secondDistributionAccountIndex.getId());
        }


        //二次分配总公式表
        //获取总公式配置信息
        SecondDistributionPlanConfigFormulaDto secondDistributionPlanConfigFormulaDto = input.getConfigFormula();
        //二次分配方案公式配置表实体对象
        SecondDistributionPlanConfigFormula secondDistributionPlanConfigFormula =
                new SecondDistributionPlanConfigFormula();

        BeanUtils.copyProperties(secondDistributionPlanConfigFormulaDto, secondDistributionPlanConfigFormula);
        secondDistributionPlanConfigFormula.setPlanCostFormula(secondDistributionPlanConfigFormulaDto.getOtherFormula().getExpression());
        secondDistributionPlanConfigFormula.setPlanId(secondDistributionAccountPlan.getId());
        secondDistributionPlanConfigFormula.setUnitId(input.getUnitId());
        secondDistributionPlanConfigFormula.setCarryRule(secondDistributionPlanConfigFormulaDto.getCarryRule());
        secondDistributionPlanConfigFormula.insert();

        //存储公式参数项
        secondDistributionPlanConfigFormulaDto.getOtherFormula().getParams().stream().forEach(r -> {
            SecondDistributionAccountFormulaParam secondDistributionAccountFormulaParam =
                    new SecondDistributionAccountFormulaParam();
            secondDistributionAccountFormulaParam.setType(SecondDistributionFormulaParamTypeEnum.ZGS.getType());
            secondDistributionAccountFormulaParam.setFormulaKey(r.getKey());
            secondDistributionAccountFormulaParam.setFormulaName(r.getName());
            secondDistributionAccountFormulaParam.setFormulaType(r.getType());
            secondDistributionAccountFormulaParam.setFormulaValue(r.getValue());
            secondDistributionAccountFormulaParam.setUnitId(input.getUnitId());
            secondDistributionAccountFormulaParam.setPlanId(secondDistributionAccountPlan.getId());
            secondDistributionAccountFormulaParam.setBizId(secondDistributionPlanConfigFormula.getId());
            secondDistributionAccountFormulaParam.insert();
        });

        //方案配置表设置核算指标信息和总公式信息
        secondDistributionAccountPlan.setAccountIndexIds(Joiner.on(",").join(accountIndexIds));
        secondDistributionAccountPlan.setFormulaIds(secondDistributionPlanConfigFormula.getId().toString());
        //生效
        secondDistributionAccountPlan.setStatus(Integer.valueOf(YesNoEnum.YES.getValue()));
        //更新方案配置表
        secondDistributionAccountPlan.updateById();
    }
}
