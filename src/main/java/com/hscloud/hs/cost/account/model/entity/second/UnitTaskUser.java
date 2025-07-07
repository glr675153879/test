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
@Schema(description = "发放单元任务人员")
@TableName("sec_unit_task_user")
public class UnitTaskUser extends BaseEntity<UnitTaskUser> {

    private static final long serialVersionUID = 1L;

    @Column(comment = "发放单元任务id")
    @Schema(description = "发放单元任务id")
    private Long unitTaskId;

    @Column(comment = "姓名")
    @Schema(description = "姓名")
    private String empName;

    @Column(comment = "基础平台userId")
    @Schema(description = "基础平台userId")
    private String userId;

    @Column(comment = "工号")
    @Schema(description = "工号")
    private String empCode;

    @Column(comment = "科室")
    @Schema(description = "科室")
    private String deptName;

    @Column(comment = "职务")
    @Schema(description = "职务")
    private String postName;

    @Column(comment = "排序号", decimalLength = 2)
    @Schema(description = "排序号")
    private Float sortNum;

    //平均绩效
    @Column(comment = "绩效倍数",decimalLength = 6,length = 15)
    @Schema(description = "绩效倍数")
    private BigDecimal avgRate = BigDecimal.ONE;

    @Column(comment = "人员系数",decimalLength = 6,length = 15)
    @Schema(description = "人员系数")
    private BigDecimal userRate = BigDecimal.ONE;

    @Column(comment = "出勤天数",decimalLength = 6,length = 15)
    @Schema(description = "出勤天数")
    private BigDecimal workdays = BigDecimal.ZERO;

    @Column(comment = "是否拿奖金")
    @Schema(description = "是否拿奖金")
    private String ifGetAmt;

    //冗余
    @Column(comment = "发放单元id")
    @Schema(description = "发放单元id")
    private Long grantUnitId;

    @Column(comment = "发放单元名称")
    @Schema(description = "发放单元名称")
    private String grantUnitName;

    // 科室二次分配
    @Deprecated
    @Column(comment = "工作量系数", decimalLength = 6, length = 15)
    @Schema(description = "工作量系数")
    private BigDecimal workRate = BigDecimal.ONE;

    @Deprecated
    @Column(comment = "工作量系数", decimalLength = 6, length = 15)
    @Schema(description = "考核得分")
    private BigDecimal examPoint = new BigDecimal("100");

}
