package com.hscloud.hs.cost.account.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.constant.enums.SecondDistributionFormulaParamCalTypeEnum;
import com.hscloud.hs.cost.account.mapper.SecondDistributionPlanConfigFormulaMapper;
import com.hscloud.hs.cost.account.model.entity.SecondDistributionAccountIndex;
import com.hscloud.hs.cost.account.model.entity.SecondDistributionPlanConfigFormula;
import com.hscloud.hs.cost.account.model.pojo.SecondDistributionFormula;
import com.hscloud.hs.cost.account.service.ISecondDistributionAccountIndexService;
import com.hscloud.hs.cost.account.service.ISecondDistributionPlanConfigFormulaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 二次分配方案公式配置表 服务实现类
 * </p>
 *
 * @author 
 * @since 2023-11-17
 */
@Service
public class SecondDistributionPlanConfigFormulaServiceImpl extends ServiceImpl<SecondDistributionPlanConfigFormulaMapper, SecondDistributionPlanConfigFormula> implements ISecondDistributionPlanConfigFormulaService {

    @Autowired
    private ISecondDistributionAccountIndexService secondDistributionAccountIndexService;

    @Override
    public List<SecondDistributionFormula.FormulaParam> getAccountIndexSelect(List<Long> input) {
        List<SecondDistributionFormula.FormulaParam> res = new ArrayList<>();

        List<SecondDistributionAccountIndex> frontSecondDistributionAccountIndex
                = new ArrayList<>();
        input.stream().forEach(r ->{
            SecondDistributionAccountIndex secondDistributionAccountIndex =
                    secondDistributionAccountIndexService.getById(r);
            frontSecondDistributionAccountIndex.add(secondDistributionAccountIndex);
        });

        frontSecondDistributionAccountIndex.stream().forEach(r -> {
            SecondDistributionFormula.FormulaParam formulaParam = new SecondDistributionFormula.FormulaParam();
            JSONObject accountIndex = JSON.parseObject(r.getAccountIndex());
            formulaParam.setName(accountIndex.getString("label"));
            formulaParam.setType(SecondDistributionFormulaParamCalTypeEnum.QZHSZBZ.getType());
            formulaParam.setValue(r.getId().toString());

            res.add(formulaParam);
        });
        return res;
    }

}
