package com.hscloud.hs.cost.account.model.entity.dataReport;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import com.hscloud.hs.cost.account.constant.enums.dataReport.AccountUnitTypeEnum;
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
@Schema(description = "上报详情基本信息表")
@Entity
@TenantTable
@Table(name = "cost_report_detail_info")
public class CostReportDetailInfo extends Model<CostReportDetailInfo> {

    @Schema(description = "上报详情信息id")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "我的上报id")
    private Long recordId;

    @Schema(description = "核算单元")
    private String measureUnit;

    @Schema(description = "归集科室")
    private String clusterUnits;

    @Schema(description = "核算分组")
    private String measureGroup;

    @Schema(description = "人员")
    private String user;

    @Schema(description = "工号")
    private String jobNumber;

    @Schema(description = "科别")
    private String deptType;

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

    @Column(comment = "备注", type = MySqlTypeConstant.TEXT)
    @Schema(description = "备注")
    private String note;

    /**
     * 核算单元类型
     * {@link AccountUnitTypeEnum}
     */
    @Column(comment = "核算单元类型", length = 1)
    @Schema(description = "核算单元类型 0科室单元 1归集单元 2核算人员")
    private String accountingUnitType;

    /**
     * 租户ID
     */
    @Schema(description = "租户ID")
    private Long tenantId;
}
