package com.hscloud.hs.cost.account.model.entity.report;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import com.hscloud.hs.cost.account.model.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;


/**
 * 报表设计表
 *
 * @author zyj
 * @date 2024/04/28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "数据集设计表")
@TableName("rp_report_db")
public class ReportDb extends BaseEntity<ReportDb> {

    @Column(comment = "报表id")
    @Schema(description = "报表id")
    private Long reportId;

    @Column(comment = "数据集类型 1:sql 2:api")
    @Schema(description = "数据集类型")
    private String dbType;

    @Column(comment = "数据集名称")
    @Schema(description = "数据集名称")
    private String dbName;

    @Column(comment = "动态查询SQL", type = MySqlTypeConstant.TEXT)
    @Schema(description = "动态查询SQL")
    private String dbDynSql;

    @Column(comment = "是否分页,0:不分页（默认） 1:分页")
    @Schema(description = "是否分页,0:不分页（默认） 1:分页")
    private String isPage;

    @TableField(exist = false)
    @Schema(description = "字段")
    private List<ReportField> reportFields;

    @TableField(exist = false)
    @Schema(description = "数据集入参")
    private List<ReportDbParam> reportDbParams;

    @TableField(exist = false)
    @Schema(description = "数据集字段")
    private List<ReportField> dbFields;

    @TableField(exist = false)
    @Schema(description = "自定义标题字段")
    private List<ReportField> headFields;

    @TableField(exist = false)
    @Schema(description = "自定义计算字段")
    private List<ReportField> calcFields;

}
