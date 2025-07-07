package com.hscloud.hs.cost.account.model.entity.second;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * @author hf
 * @since 2023-10-04
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "任务科室二次分配工作量系数")
@TableName("sec_unit_task_detail_item_work")
public class UnitTaskDetailItemWork extends ProgProjectDetail {

    private static final long serialVersionUID = 1L;

    @Column(comment = "明细标值id")
    @Schema(description = "明细标值id")
    private Long unitTaskProjectDetailId;

    @Column(comment = "工号")
    @Schema(description = "工号")
    private String empCode;

    @Column(comment = "工作量系数", decimalLength = 6, length = 15)
    @Schema(description = "工作量系数")
    private BigDecimal workRate = BigDecimal.ONE;

    @Column(comment = "考核得分", decimalLength = 6, length = 15)
    @Schema(description = "考核得分")
    private BigDecimal examPoint = new BigDecimal("100");

    // 冗余
    @Column(comment = "核算指标id")
    @Schema(description = "核算指标id")
    private Long unitTaskProjectId;

    // 冗余
    @Column(comment = "发放单元任务id")
    @Schema(description = "发放单元任务id")
    private Long unitTaskId;

}
