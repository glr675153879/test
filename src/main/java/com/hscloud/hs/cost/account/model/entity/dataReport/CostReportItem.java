package com.hscloud.hs.cost.account.model.entity.dataReport;


import com.baomidou.mybatisplus.annotation.*;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import com.pig4cloud.pigx.common.core.util.TenantTable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "上报项")
@Entity
@TenantTable
@Table(name = "cost_report_item")
public class CostReportItem extends Model<CostReportItem> {

    @Schema(description = "上报项id")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "上报项名称")
    private String name;

    @Schema(description = "上报项单位")
    private String measureUnit;

    @Schema(description = "口径颗粒度 1全院 2核算单元 3核算单元+人员 4人员 5RW(作废) 6固定值（成本科室） 7核算单元（成本科室）")
    private String reportType;

    @Schema(description = "数据类型 0正 1负 2不限")
    private String dataType;

    @Schema(description = "上报项说明")
    private String description;

    @Schema(description = "是否区分科别 0否 1是")
    private String isDeptDistinguished;

    @Column(comment = "业务类型: 0绩效 1科室成本", length = 1,
            type = MySqlTypeConstant.VARCHAR, defaultValue = "0")
    @Schema(description = "业务类型: 0绩效 1科室成本")
    private String type = "0";

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "删除标记,1:已删除,0:正常")
    private String delFlag;

    @Schema(description = "状态：0：停用 1: 启用")
    private String status;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建人")
    private String createBy;

    @TableField(fill = FieldFill.UPDATE)
    @Schema(description = "修改人")
    private String updateBy;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.UPDATE)
    @Schema(description = "修改时间")
    private LocalDateTime updateTime;

    @Schema(description = "租户id")
    private Long tenantId;
}
