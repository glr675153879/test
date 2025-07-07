package com.hscloud.hs.cost.account.model.entity.report;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.hscloud.hs.cost.account.model.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * 数据集字段自定义公式 包含的字段
 *
 * @author zyj
 * @date 2024/04/28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "数据集字段自定义公式包含的字段")
@TableName("rp_report_field_formula_details")
public class ReportFieldFormulaDetails extends BaseEntity<ReportFieldFormulaDetails> {

    @Column(comment = "公式id")
    @Schema(description = "公式id")
    private Long reportFormulaId;

    @Column(comment = "字段id")
    @Schema(description = "字段id")
    private Long reportFieldId;

    @Column(comment = "变量")
    @Schema(description = "变量")
    private String reportFieldName;

    @TableField(exist = false)
    @Schema(description = "报表别名")
    private String reportFieldViewAlias;

    @TableField(exist = false)
    @Schema(description = "值")
    private Object reportFieldValue;

}
