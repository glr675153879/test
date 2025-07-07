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
@Schema(description = "发放单元任务")
@TableName("sec_unit_task")
public class UnitTask extends BaseEntity<UnitTask> {

    private static final long serialVersionUID = 1L;

    @Column(comment = "二次分配任务id")
    @Schema(description = "二次分配任务id")
    private Long secondTaskId;

    @Column(comment = "方案id")
    @Schema(description = "方案id")
    private Long programmeId;

    @Column(comment = "是否上报方式")
    @Schema(description = "是否上报方式")
    private String ifUpload;

    @Column(comment = "名称")
    @Schema(description = "名称")
    private String name;

    @Column(comment = "发放单元id")
    @Schema(description = "发放单元id")
    private Long grantUnitId;

    @Column(comment = "发放单元名称")
    @Schema(description = "发放单元名称")
    private String grantUnitName;

    @Column(comment = "负责人id")
    @Schema(description = "负责人id 1,2,3")
    private String leaderIds;

    @Column(comment = "负责人名称")
    @Schema(description = "负责人名称 a,b,c")
    private String leaderNames;

    @Column(comment = "科室绩效",decimalLength = 5,length = 15)
    @Schema(description = "科室绩效")
    private BigDecimal ksAmt = BigDecimal.ZERO;

    @Column(comment = "专项绩效",decimalLength = 5,length = 15)
    @Schema(description = "专项绩效")
    private BigDecimal zxAmt = BigDecimal.ZERO;

    @Column(comment = "分配周期")
    @Schema(description = "分配周期")
    private String cycle;

    @Column(comment = "下发时间")
    @Schema(description = "下发时间")
    private LocalDateTime startTime;

    @Column(comment = "流程提交时间")
    @Schema(description = "流程提交时间")
    private LocalDateTime submitTime;

    @Column(comment = "审批完成时间")
    @Schema(description = "审批完成时间")
    private LocalDateTime endTime;

    @Column(comment = "状态 SecondDistributionTaskStatusEnum")
    @Schema(description = "状态 SecondDistributionTaskStatusEnum")
    private String status;

    @Column(comment = "是否完成人员设置")
    @Schema(description = "是否完成人员设置")
    private String ifSetUser;

    @Column(comment = "是否完成")
    @Schema(description = "是否完成")
    private String ifFinish;

    @Column(comment = "当前分配节点 ：0人员配置  99分配结果查看")
    @Schema(description = "当前分配节点 ：0人员配置  99分配结果查看")
    private Long focusProjectId = 0L;

    @Column(comment = "审批流程id")
    @Schema(description = "审批流程id")
    private Long instanceId ;

}
