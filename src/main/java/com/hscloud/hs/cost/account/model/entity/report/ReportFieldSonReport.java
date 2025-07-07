package com.hscloud.hs.cost.account.model.entity.report;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import com.hscloud.hs.cost.account.model.entity.CostAccountUnit;
import com.hscloud.hs.cost.account.model.entity.base.BaseEntity;
import com.hscloud.hs.cost.account.model.pojo.report.ParamMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;


/**
 * 链接报表列表
 *
 * @author zyj
 * @date 2024/04/28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "链接报表列表")
@TableName("rp_report_field_son_report")
public class ReportFieldSonReport extends BaseEntity<ReportFieldSonReport> {

    @Column(comment = "数据集字段id")
    @Schema(description = "数据集字段id")
    private Long reportFieldId;

    @Column(comment = "子报表类型 1：所有核算单元 2：指定核算单元")
    @Schema(description = "子报表类型 1：所有核算单元 2：指定核算单元")
    private String sonReportType;

    @Column(comment = "匹配字段", type = MySqlTypeConstant.LONGTEXT)
    @Schema(description = "匹配字段")
    private String matchFieldName;

    @Column(comment = "核算单元ids", type = MySqlTypeConstant.LONGTEXT)
    @Schema(description = "核算单元ids")
    private String matchFieldValues;

    @Column(comment = "核算单元文本", type = MySqlTypeConstant.LONGTEXT)
    @Schema(description = "核算单元文本")
    private String matchFieldTexts;

    @Column(comment = "子报表id")
    @Schema(description = "子报表id")
    private Long sonReportId;

    @Column(comment = "参数映射 父报表存字段id [{\"parentId\",\"childCode\",\"parentText\",\"childText\"}]", length = 2000)
    @Schema(description = "参数映射 父报表存字段id [{\"parentId\",\"childCode\",\"parentText\",\"childText\"}]")
    private String paramMappingJson;

    @Schema(description = "核算单元list")
    @TableField(exist = false)
    private List<CostAccountUnit> accountUnitList;

    @Schema(description = "参数映射list")
    @TableField(exist = false)
    private List<ParamMapping> paramMappingList;

    @TableField(exist = false)
    @Schema(description = "子报表code")
    private String sonReportCode;

    @Column(comment = "子报表名称")
    @Schema(description = "子报表名称")
    private String sonReportName;

}
