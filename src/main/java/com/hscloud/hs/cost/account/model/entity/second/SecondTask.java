package com.hscloud.hs.cost.account.model.entity.second;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.hscloud.hs.cost.account.model.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author hf
 * @since 2023-10-04
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "二次分配总任务")
@TableName("sec_second_task")
public class SecondTask extends BaseEntity<SecondTask> {

    private static final long serialVersionUID = 1L;

    @Column(comment = "一次分配任务id")
    @Schema(description = "一次分配任务id")
    private Long firstTaskId;

    @Column(comment = "名称")
    @Schema(description = "名称")
    private String name;

    @Column(comment = "科室绩效")
    @Schema(description = "科室绩效")
    private BigDecimal ksAmt = BigDecimal.ZERO;

    @Column(comment = "专项绩效")
    @Schema(description = "专项绩效")
    private BigDecimal zxAmt = BigDecimal.ZERO;

    @Column(comment = "分配周期")
    @Schema(description = "分配周期")
    private String cycle;

    @Column(comment = "下发时间")
    @Schema(description = "下发时间")
    private LocalDateTime startTime;

    @Column(comment = "完成时间")
    @Schema(description = "完成时间")
    private LocalDateTime endTime;

    @Column(comment = "状态：进行中：UNDERWAY、已完成：FINISHED")
    @Schema(description = "状态：进行中：UNDERWAY、已完成：FINISHED")
    private String status;

    @Column(comment = "发放单元id")
    @Schema(description = "发放单元id")
    private String grantUnitIds;

    @Column(comment = "发放单元名称")
    @Schema(description = "发放单元名称")
    private String grantUnitNames;
}
