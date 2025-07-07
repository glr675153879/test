package com.hscloud.hs.cost.account.service.impl.report;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.report.ReportFieldFormulaMapper;
import com.hscloud.hs.cost.account.model.entity.CostAccountUnit;
import com.hscloud.hs.cost.account.model.entity.report.ReportFieldFormula;
import com.hscloud.hs.cost.account.model.entity.report.ReportFieldFormulaDetails;
import com.hscloud.hs.cost.account.service.report.IReportFieldFormulaDetailsService;
import com.hscloud.hs.cost.account.service.report.IReportFieldFormulaService;
import com.hscloud.hs.cost.account.utils.CommonUtils;
import com.pig4cloud.pigx.common.core.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 数据集字段自定义公式 服务实现类
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportFieldFormulaService extends ServiceImpl<ReportFieldFormulaMapper, ReportFieldFormula> implements IReportFieldFormulaService {

    private final IReportFieldFormulaDetailsService reportFieldFormulaDetailsService;

    @Lazy
    @Autowired
    private ReportFieldFormulaService reportFieldFormulaService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrEdit(ReportFieldFormula reportFieldFormula) {
        this.validData(reportFieldFormula);
        //将accountUnitList转换为matchFieldValues matchFieldTexts
        convert(reportFieldFormula);
        //保存公式
        this.saveOrUpdate(reportFieldFormula);
        //保存公式包含字段
        reportFieldFormulaDetailsService.saveList(reportFieldFormula);
    }

    @Override
    public List<ReportFieldFormula> listByFieldId(Long reportField) {
        return super.list(Wrappers.<ReportFieldFormula>lambdaQuery().eq(ReportFieldFormula::getReportFieldId, reportField));
    }

    // @Cacheable(cacheManager = "localCacheManager", value = "reportFieldFormula", key = "#fieldId+'_'+#accountUnitCode", sync = true)
    @Override
    public ReportFieldFormula findByFieldId(Long fieldId, String accountUnitCode) {
        if (accountUnitCode == null) {
            return this.getCommonByFieldId(fieldId);
        }
        ReportFieldFormula reportFieldFormula = this.getOne(Wrappers.<ReportFieldFormula>lambdaQuery()
                .eq(ReportFieldFormula::getReportFieldId, fieldId)
                .eq(ReportFieldFormula::getFormulaType, "2")
                .like(ReportFieldFormula::getMatchFieldValues, accountUnitCode));
        if (reportFieldFormula == null) {
            reportFieldFormula = reportFieldFormulaService.getCommonByFieldId(fieldId);
        }
        return reportFieldFormula;
    }

    /**
     * 删除公式
     * 级联删除公式相关字段
     *
     * @param id 报表字段公式
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteById(Long id) {
        super.removeById(id);
        //删除公式详情
        reportFieldFormulaDetailsService.remove(Wrappers.<ReportFieldFormulaDetails>lambdaQuery().eq(ReportFieldFormulaDetails::getReportFormulaId, id));
        return true;
    }

    @Override
    public List<ReportFieldFormula> listByFieldIds(Collection<Long> fieldIds) {
        if (CollUtil.isEmpty(fieldIds)) {
            return new ArrayList<>();
        }
        return super.list(Wrappers.<ReportFieldFormula>lambdaQuery().in(ReportFieldFormula::getReportFieldId, fieldIds));
    }

    @Override
    public boolean existsByFieldId(Long fieldId) {
        return super.exists(Wrappers.<ReportFieldFormula>lambdaQuery().eq(ReportFieldFormula::getReportFieldId, fieldId));
    }

    private static void convert(ReportFieldFormula reportFieldFormula) {
        List<CostAccountUnit> accountUnitList = reportFieldFormula.getAccountUnitList();
        if (CollUtil.isNotEmpty(accountUnitList)) {
            List<Long> matchFieldValues = new ArrayList<>();
            List<String> matchFieldTexts = new ArrayList<>();
            accountUnitList.forEach(costAccountUnit -> {
                matchFieldValues.add(costAccountUnit.getId());
                matchFieldTexts.add(costAccountUnit.getName());
            });
            reportFieldFormula.setMatchFieldValues(StrUtil.join(",", matchFieldValues));
            reportFieldFormula.setMatchFieldTexts(StrUtil.join(",", matchFieldTexts));
        }
        String expression = reportFieldFormula.getExpression();
        if (StrUtil.isNotBlank(expression)) {
            String expressionJs = expression.replaceAll("=", "==")
                    .replaceAll("%", "/100")
                    .replaceAll("≥", ">=")
                    .replaceAll("≤", "<=>");
            reportFieldFormula.setExpressionJs(expressionJs);
        }

    }

    public static void main(String[] args) throws ScriptException {
        ReportFieldFormula rf = new ReportFieldFormula();

        String a = "22/3+30%+IF(AND(1≥2,1=1,2=2),100,MAX(0.1,0.2,0.3))+MIN(11,22,9)+MAX(0.1,0.2,0.3);";
        a = "MAX(1,2,3) + MIN(11,22,33%)+IF(OR(1≥2,1=1,2=2),100,MAX(0.1,0.2,0.3))";
        rf.setExpression(a);
        convert(rf);
        a = rf.getExpressionJs();
        System.out.println(a);
        System.out.println(CommonUtils.caclByEval(a));
    }

    // @Cacheable(cacheManager = "localCacheManager", value = "reportFieldCommonFormula", key = "#fieldId", sync = true)
    public ReportFieldFormula getCommonByFieldId(Long fieldId) {
        return this.getOne(Wrappers.<ReportFieldFormula>lambdaQuery()
                .eq(ReportFieldFormula::getReportFieldId, fieldId)
                .eq(ReportFieldFormula::getFormulaType, "1"));
    }

    private void validData(ReportFieldFormula fieldFormula) {
        Long reportFieldId = fieldFormula.getReportFieldId();
        if (Objects.isNull(reportFieldId)) {
            throw new BizException("报表字段ID不能为空");
        }
        String formulaType = fieldFormula.getFormulaType();
        if (StrUtil.isBlank(formulaType)) {
            throw new BizException("适用核算单元类型不能为空");
        }
        if(!StrUtil.equalsAny(formulaType, "1", "2")){
            throw new BizException("适用核算单元类型不正确");
        }
        List<CostAccountUnit> accountUnitList = fieldFormula.getAccountUnitList();
        if (Objects.equals("2", formulaType) && CollUtil.isEmpty(accountUnitList)) {
            throw new BizException("未选择核算单元");
        }
        if (Objects.equals("1", formulaType)) {
            //如果已经存在全部核算单元 formulaType=1的情况，则不允许在添加
            boolean isExistAll = super.exists(Wrappers.<ReportFieldFormula>lambdaQuery()
                    .eq(ReportFieldFormula::getReportFieldId, fieldFormula.getReportFieldId())
                    .eq(ReportFieldFormula::getFormulaType, "1")
                    .ne(Objects.nonNull(fieldFormula.getId()), ReportFieldFormula::getId, fieldFormula.getId()));
            if (isExistAll) {
                throw new BizException("不允许添加多个[全部核算单元]类型公式");
            }
        }
        if (Objects.equals("2", formulaType)) {
            //不允许一个核算单元重复添加在多个子报表
            List<String> existAccountName = new ArrayList<>();
            for (CostAccountUnit costAccountUnit : accountUnitList) {
                boolean isExist = super.exists(Wrappers.<ReportFieldFormula>lambdaQuery()
                        .eq(ReportFieldFormula::getReportFieldId, fieldFormula.getReportFieldId())
                        .eq(ReportFieldFormula::getFormulaType, formulaType)
                        .like(ReportFieldFormula::getMatchFieldValues, costAccountUnit.getId())
                        .ne(Objects.nonNull(fieldFormula.getId()), ReportFieldFormula::getId, fieldFormula.getId()));
                if (isExist) {
                    existAccountName.add(costAccountUnit.getName());
                }
            }
            if (CollUtil.isNotEmpty(existAccountName)) {
                throw new BizException(String.format("核算单元%s已在其他公式中配置", StrUtil.join(",", existAccountName)));
            }
        }
        //TODO:保存包含字段的时候校验和公式是否吻合

    }

}
