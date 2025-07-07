package com.hscloud.hs.cost.account.model.entity.second;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.hscloud.hs.cost.account.model.entity.base.BaseEntity;
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
@Schema(description = "核算指标分配结果按人汇总")
@TableName("sec_unit_task_project_count")
public class UnitTaskProjectCount extends BaseEntity<UnitTaskProjectCount> {

    private static final long serialVersionUID = 1L;

    @Column(comment = "核算指标id")
    @Schema(description = "核算指标id")
    private Long projectId;

    @Column(comment = "核算指标名称")
    @Schema(description = "核算指标名称")
    private String projectName;

    @Column(comment = "职工姓名")
    @Schema(description = "职工姓名")
    private String empName;

    @Column(comment = "工号")
    @Schema(description = "工号")
    private String empCode;

    @Column(comment = "userId")
    @Schema(description = "userId")
    private Long userId;

    @Column(comment = "绩效金额",decimalLength = 6,length = 15)
    @Schema(description = "绩效金额")
    private BigDecimal amt = BigDecimal.ZERO;

    @Column(comment = "排序号")
    @Schema(description = "排序号")
    private Integer sortNum;

    //冗余
    @Column(comment = "二次分配任务id")
    @Schema(description = "二次分配任务id")
    private Long secondTaskId;

    @Column(comment = "发放单元分配任务id")
    @Schema(description = "发放单元分配任务id")
    private Long unitTaskId;

}
