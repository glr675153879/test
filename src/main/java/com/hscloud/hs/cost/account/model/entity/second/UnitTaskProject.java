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
@Schema(description = "任务核算指标")
@TableName("sec_unit_task_project")
public class UnitTaskProject extends ProgProject {

    private static final long serialVersionUID = 1L;

    @Column(comment = "发放单元任务id")
    @Schema(description = "发放单元任务id")
    private Long unitTaskId;

    @Column(comment = "关联方案核算指标id")
    @Schema(description = "关联方案核算指标id")
    private Long progProjectId;

    @Column(comment = "剩余可分配金额",decimalLength = 5,length = 15)
    @Schema(description = "剩余可分配金额")
    private BigDecimal beforeAmt = BigDecimal.ZERO;

    @Column(comment = "本页分配金额",decimalLength = 5,length = 15)
    @Schema(description = "本页分配金额")
    private BigDecimal countAmt = BigDecimal.ZERO;

    @Column(comment = "本页后剩余可分配金额",decimalLength = 5,length = 15)
    @Schema(description = "本页后剩余可分配金额")
    private BigDecimal afterAmt = BigDecimal.ZERO;

    @Column(comment = "下个核算指标id")
    @Schema(description = "下个核算指标id")
    private Long nextProjectId;

    @Column(comment = "是否已编辑")
    @Schema(description = "是否已编辑")
    private String ifEdited;

    @TableField(exist = false)
    List<UnitTaskProjectDetail> unitTaskProjectDetailList = new ArrayList<>();

}
