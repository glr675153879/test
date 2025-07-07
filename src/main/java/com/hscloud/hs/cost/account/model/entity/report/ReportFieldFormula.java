package com.hscloud.hs.cost.account.model.entity.report;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import com.hscloud.hs.cost.account.model.entity.CostAccountUnit;
import com.hscloud.hs.cost.account.model.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;


/**
 * 数据集字段自定义公式
 *
 * @author zyj
 * @date 2024/04/28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "数据集字段自定义公式")
@TableName("rp_report_field_formula")
public class ReportFieldFormula extends BaseEntity<ReportFieldFormula> {

    @Column(comment = "自定义字段id")
    @Schema(description = "自定义字段id")
    private Long reportFieldId;

    @Column(comment = "公式类型 1：所有核算单元 2：指定核算单元")
    @Schema(description = "公式类型 1：所有核算单元 2：指定核算单元")
    private String formulaType;

    @Column(comment = "保留小数位数")
    @Schema(description = "保留小数位数")
    private Integer reservedDecimal;

    /**
     * 核算单元对应的字段英文名
     */
    @Column(comment = "匹配字段", type = MySqlTypeConstant.LONGTEXT)
    @Schema(description = "匹配字段")
    private String matchFieldName;

    @Column(comment = "核算单元ids", type = MySqlTypeConstant.LONGTEXT)
    @Schema(description = "核算单元ids")
    private String matchFieldValues;

    @Column(comment = "核算单元文本", type = MySqlTypeConstant.LONGTEXT)
    @Schema(description = "核算单元文本")
    private String matchFieldTexts;

    @Column(comment = "前端表达式", length = 2000)
    @Schema(description = "前端表达式")
    private String expression;

    @Column(comment = "计算表达式", length = 2000)
    @Schema(description = "计算表达式")
    private String expressionJs;

    @TableField(exist = false)
    @Schema(description = "公式包含字段列表")
    private List<ReportFieldFormulaDetails> paramsList;

    @Schema(description = "核算单元list")
    @TableField(exist = false)
    private List<CostAccountUnit> accountUnitList;

    @TableField(exist = false)
    @Schema(description = "报表字段标题")
    private String fieldViewAlias;

}
