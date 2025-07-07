package com.hscloud.hs.cost.account.model.entity.dataReport;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "上报详情-费用表")
@Entity
@Table(name = "cost_report_detail_cost")
public class CostReportDetailCost extends Model<CostReportDetailCost> {

    private static final long serialVersionUID = -3990093806613488240L;
    @Schema(description = "上报详情费用id")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "上报详情费用名称")
    private String name;

    @Schema(description = "我的上报id")
    private Long recordId;

    @Schema(description = "上报项id")
    private Long itemId;

    @Schema(description = "上报详情信息id")
    private Long detailInfoId;

    @Schema(description = "上报项单位")
    private String measureUnit;

    @Schema(description = "数据类型 0正 1负 2不限")
    private String dataType;

    @Column(comment = "上报值", decimalLength = 6, length = 16)
    @Schema(description = "上报值")
    private BigDecimal amt = BigDecimal.ZERO;

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

    /**
     * 租户ID
     */
    @Schema(description = "租户ID")
    private Long tenantId;
}
