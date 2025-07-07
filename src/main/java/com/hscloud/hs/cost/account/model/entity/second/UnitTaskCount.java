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
@Schema(description = "发放单元分配结果按人汇总")
@TableName("sec_unit_task_count")
public class UnitTaskCount extends BaseEntity<UnitTaskCount> {

    private static final long serialVersionUID = 1L;

    @Column(comment = "发放单元分配任务id")
    @Schema(description = "发放单元分配任务id")
    private Long unitTaskId;

    @Column(comment = "职工姓名")
    @Schema(description = "职工姓名")
    private String empName;

    @Column(comment = "工号")
    @Schema(description = "工号")
    private String empCode;

    @Column(comment = "userId")
    @Schema(description = "userId")
    private Long userId;

    @Column(comment = "绩效金额",decimalLength = 5,length = 15)
    @Schema(description = "绩效金额")
    private BigDecimal amt = BigDecimal.ZERO;

    //冗余
    @Column(comment = "二次分配任务id")
    @Schema(description = "二次分配任务id")
    private Long secondTaskId;


}
