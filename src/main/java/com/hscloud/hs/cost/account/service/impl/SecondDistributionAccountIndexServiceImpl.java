package com.hscloud.hs.cost.account.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.constant.enums.SecondDistributionFormulaParamCalTypeEnum;
import com.hscloud.hs.cost.account.constant.enums.SecondDistributionIndexEnum;
import com.hscloud.hs.cost.account.constant.enums.SecondDistributionSelectIndexDictTypeEnum;
import com.hscloud.hs.cost.account.constant.enums.UnitMapEnum;
import com.hscloud.hs.cost.account.mapper.SecondDistributionAccountIndexMapper;
import com.hscloud.hs.cost.account.model.entity.CostAccountUnit;
import com.hscloud.hs.cost.account.model.entity.SecondDistributionAccountIndex;
import com.hscloud.hs.cost.account.model.entity.SecondDistributionSettingsSingle;
import com.hscloud.hs.cost.account.model.entity.SecondDistributionSettingsWorkload;
import com.hscloud.hs.cost.account.model.pojo.SecondDistributionFormula;
import com.hscloud.hs.cost.account.service.*;
import com.pig4cloud.pigx.admin.api.entity.SysDictItem;
import com.pig4cloud.pigx.admin.api.feign.RemoteDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 二次分配方案核算指标表 服务实现类
 * </p>
 *
 * @author 
 * @since 2023-11-24
 */
@Service
public class SecondDistributionAccountIndexServiceImpl extends ServiceImpl<SecondDistributionAccountIndexMapper, SecondDistributionAccountIndex> implements ISecondDistributionAccountIndexService {

    @Autowired
    private ISecondDistributionSettingsManagementService secondDistributionSettingsManagementService;

    @Autowired
    private ISecondDistributionAccountFormulaParamService secondDistributionAccountFormulaParamService;

    @Autowired
    private ISecondDistributionSettingsSingleService secondDistributionSettingsSingleService;

    @Autowired
    private ISecondDistributionSettingsWorkloadService secondDistributionSettingsWorkloadService;

    @Autowired
    @Lazy
    private ISecondDistributionAccountPlanService secondDistributionAccountPlanService;

    @Autowired
    private CostAccountUnitService costAccountUnitService;

    @Autowired
    private RemoteDictService remoteDictService;

    @Override
    public List<SecondDistributionFormula.FormulaParam>
    getAccountIndexSelect(String accountIndex, Long unitId) {
        //传参声明
        List<SecondDistributionFormula.FormulaParam> res = new ArrayList<>();

        //处理当前的
        if(SecondDistributionIndexEnum.DXJX.getItem().equals(accountIndex)){
            //单项绩效: 前置核算指标 + 单项绩效（分配设置）
            //根据单项绩效存储的id，在方案管理中找出到对应的值，放入公式中进行计算

            //处理之后的
            List<SecondDistributionSettingsSingle> lists =
                    secondDistributionSettingsSingleService
                    .list(new LambdaQueryWrapper<SecondDistributionSettingsSingle>()
                    .eq(SecondDistributionSettingsSingle::getUnitId, unitId));

            lists.stream().forEach(r ->{
                SecondDistributionFormula.FormulaParam formulaParam =
                        new SecondDistributionFormula.FormulaParam();
                formulaParam.setName(r.getName());
                formulaParam.setType(SecondDistributionFormulaParamCalTypeEnum.FPSZ.getType());
                formulaParam.setValue(r.getId().toString());
                res.add(formulaParam);
            });

        }else if(SecondDistributionIndexEnum.GRZCXS.getItem().equals(accountIndex)){
            //个人职称绩效: 前置核算指标 + 对应固定的指标（字典配置）
            //根据对应的字典去做对应的处理

            //获取固定的
            List<SysDictItem> data = remoteDictService.getDictByType(SecondDistributionSelectIndexDictTypeEnum
                    .GRGWJX.getType()).getData();
            data.forEach(r -> {
                SecondDistributionFormula.FormulaParam formulaParam = new SecondDistributionFormula.FormulaParam();
                formulaParam.setName( r.getLabel());
                formulaParam.setType(SecondDistributionFormulaParamCalTypeEnum.GDZDZB.getType());
                formulaParam.setValue(r.getItemValue());
                res.add(formulaParam);
            });

        }else if(SecondDistributionIndexEnum.GZLJX.getItem().equals(accountIndex)){
            //工作量绩效： 前置核算指标 + 工作量绩效（分配设置）
            //类似于单项绩效

            //处理之后的
            List<SecondDistributionSettingsWorkload> lists = secondDistributionSettingsWorkloadService
                    .list(new LambdaQueryWrapper<SecondDistributionSettingsWorkload>()
                            .eq(SecondDistributionSettingsWorkload::getUnitId, unitId));

            lists.stream().forEach(r ->{
                SecondDistributionFormula.FormulaParam formulaParam = new SecondDistributionFormula.FormulaParam();
                formulaParam.setName(r.getName());
                formulaParam.setType(SecondDistributionFormulaParamCalTypeEnum.FPSZ.getType());
                formulaParam.setValue(r.getId().toString());
                res.add(formulaParam);
            });
        }else if(SecondDistributionIndexEnum.PJFPJX.getItem().equals(accountIndex)){
            //平均分配系数 前置 + 对应固定的指标（字典配置）
            //根据对应的字典去做对应的处理

            //根据核算单元id获取对应的固定指标
            CostAccountUnit costAccountUnit = costAccountUnitService.getOne(new LambdaQueryWrapper<CostAccountUnit>()
                    .eq(CostAccountUnit::getId, unitId));
            JSONObject  accountGroupCode = JSON.parseObject(costAccountUnit.getAccountGroupCode());
            if(UnitMapEnum.DOCKER.getUnitGroup().equals(accountGroupCode.getString("value"))){
                //医生组
                List<SysDictItem> data = remoteDictService
                        .getDictByType(SecondDistributionSelectIndexDictTypeEnum
                                .DOCPJJX.getType()).getData();
                data.forEach(r -> {
                    SecondDistributionFormula.FormulaParam formulaParam = new SecondDistributionFormula.FormulaParam();
                    formulaParam.setName( r.getLabel());
                    formulaParam.setType(SecondDistributionFormulaParamCalTypeEnum.GDZDZB.getType());
                    formulaParam.setValue(r.getItemValue());
                    res.add(formulaParam);
                });
            }else if(UnitMapEnum.NURSE.getUnitGroup().equals(accountGroupCode.getString("value"))){
                //护士组
                List<SysDictItem> data = remoteDictService
                        .getDictByType(SecondDistributionSelectIndexDictTypeEnum
                                .NURSEPJJX.getType()).getData();
                data.forEach(r -> {
                    SecondDistributionFormula.FormulaParam formulaParam = new SecondDistributionFormula.FormulaParam();
                    formulaParam.setName( r.getLabel());
                    formulaParam.setType(SecondDistributionFormulaParamCalTypeEnum.GDZDZB.getType());
                    formulaParam.setValue(r.getItemValue());
                    res.add(formulaParam);
                });

            }else if(UnitMapEnum.MEDICAL_SKILL.getUnitGroup().equals(accountGroupCode.getString("value"))){
                //医技组
                List<SysDictItem> data = remoteDictService
                        .getDictByType(SecondDistributionSelectIndexDictTypeEnum
                                .YJPJJX.getType()).getData();
                data.forEach(r -> {
                    SecondDistributionFormula.FormulaParam formulaParam = new SecondDistributionFormula.FormulaParam();
                    formulaParam.setName( r.getLabel());
                    formulaParam.setType(SecondDistributionFormulaParamCalTypeEnum.GDZDZB.getType());
                    formulaParam.setValue(r.getItemValue());
                    res.add(formulaParam);
                });
            }else if(UnitMapEnum.ADMINISTRATION.getUnitGroup().equals(accountGroupCode.getString("value"))){
                //行政组
                List<SysDictItem> data = remoteDictService
                        .getDictByType(SecondDistributionSelectIndexDictTypeEnum
                                .ADMINPJJX.getType()).getData();
                data.forEach(r -> {
                    SecondDistributionFormula.FormulaParam formulaParam = new SecondDistributionFormula.FormulaParam();
                    formulaParam.setName( r.getLabel());
                    formulaParam.setType(SecondDistributionFormulaParamCalTypeEnum.GDZDZB.getType());
                    formulaParam.setValue(r.getItemValue());
                    res.add(formulaParam);
                });
            }else{
                //其他核算单元类型
                List<SysDictItem> data = remoteDictService
                        .getDictByType(SecondDistributionSelectIndexDictTypeEnum
                                .OTHERJJX.getType()).getData();
                data.forEach(r -> {
                    SecondDistributionFormula.FormulaParam formulaParam = new SecondDistributionFormula.FormulaParam();
                    formulaParam.setName( r.getLabel());
                    formulaParam.setType(SecondDistributionFormulaParamCalTypeEnum.GDZDZB.getType());
                    formulaParam.setValue(r.getItemValue());
                    res.add(formulaParam);
                });
            }
        }
        return res;
    }

}
