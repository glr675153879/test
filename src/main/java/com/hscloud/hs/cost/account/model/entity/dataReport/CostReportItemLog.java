package com.hscloud.hs.cost.account.model.entity.dataReport;


import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import com.pig4cloud.pigx.common.core.util.TenantTable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "上报项变更日志")
@Entity
@TenantTable
@Table(name = "cost_report_item_log")
public class CostReportItemLog extends Model<CostReportItemLog> {


    /**
     * 构造器，初始化名称
     */
    public CostReportItemLog() {
        this.name = "上报项设置";
    }


    @Schema(description = "id")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "应用名称")
    private String name;

    @Schema(description = "操作类型 1：新增 2：变更 3：删除 4:启停")
    private String opsType;

    @Schema(description = "操作项")
    private String opsItem;

    @Schema(description = "操作人")
    private String opsBy;

    @Schema(description = "操作人id")
    private Long opsById;

    @Schema(description = "操作时间")
    private LocalDateTime opsTime;

    @Schema(description = "工号")
    private String jobNumber;

    @Schema(description = "描述")
    private String description;

    @Column(comment = "业务类型: 0绩效 1科室成本", length = 1,
            type = MySqlTypeConstant.VARCHAR, defaultValue = "0")
    @Schema(description = "业务类型: 0绩效 1科室成本")
    private String type = "0";

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "删除标记,1:已删除,0:正常")
    private String delFlag;

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
