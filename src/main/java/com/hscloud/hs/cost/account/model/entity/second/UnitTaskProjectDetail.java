package com.hscloud.hs.cost.account.model.entity.second;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author hf
 * @since 2023-10-04
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "任务核算指标明细值")
@TableName("sec_unit_task_project_detail")
public class UnitTaskProjectDetail extends ProgProjectDetail {

    private static final long serialVersionUID = 1L;

    @Column(comment = "核算指标id")
    @Schema(description = "核算指标id")
    private Long unitTaskProjectId;

    @Column(comment = "关联方案核算指标明细id")
    @Schema(description = "关联方案核算指标明细id")
    private Long progProjectDetailId;

    @Column(comment = "工号")
    @Schema(description = "工号")
    private String empCode;

    @Column(comment = "数量",decimalLength = 6,length = 15)
    @Schema(description = "数量")
    private BigDecimal qty = BigDecimal.ZERO;

    @Column(comment = "金额",decimalLength = 6,length = 15)
    @Schema(description = "金额")
    private BigDecimal amt = BigDecimal.ZERO;

    @Column(comment = "是否已编辑item")
    @Schema(description = "是否已编辑item")
    private String ifEdited;

    @TableField(exist = false)
    List<UnitTaskDetailItem> unitTaskDetailItemList = new ArrayList<>();

}
