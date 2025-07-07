package com.hscloud.hs.cost.account.model.entity.report;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.hscloud.hs.cost.account.model.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;


/**
 * 报表字段
 *
 * @author zyj
 * @date 2024/04/28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "报表字段")
@TableName("rp_report_field")
public class ReportField extends BaseEntity<ReportField> {

    @Column(comment = "报表id")
    @Schema(description = "报表id")
    private Long reportId;

    @Column(comment = "数据集id")
    @Schema(description = "数据集id")
    private Long reportDbId;

    @Column(comment = "变量名称")
    @Schema(description = "变量名称")
    private String fieldName;

    @Column(comment = "变量说明")
    @Schema(description = "变量说明")
    private String fieldText;

    @Column(comment = "报表字段标题")
    @Schema(description = "报表字段标题")
    private String fieldViewAlias;

    @Column(comment = "查询标识 0否1是 默认0 自定义字段强制否")
    @Schema(description = "查询标识 0否1是 自定义字段强制否")
    private String searchFlag;

    @Column(comment = "排序")
    @Schema(description = "排序")
    private Integer sort;

    @Column(comment = "字段类型 1：数据集字段 2：自定义计算字段 3：自定义标题字段")
    @Schema(description = "字段类型 1：数据集字段 2：自定义计算字段 3：自定义标题字段")
    private String fieldType;

    @TableField(exist = false)
    @Schema(description = "是否配置子报表 0：否 1：是 默认0")
    private String sonReportFlag = "0";

    @TableField(exist = false)
    @Schema(description = "是否配置公式 0：否 1：是 默认0")
    private String formulaFlag = "0";

    @TableField(exist = false)
    @Schema(description = "自定义字段公式列表")
    private List<ReportFieldFormula> reportFieldFormulas;

    @TableField(exist = false)
    @Schema(description = "链接报表列表")
    private List<ReportFieldSonReport> sonReports;

}
