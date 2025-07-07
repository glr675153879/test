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
@Schema(description = "任务科室二次分配明细大项值")
@TableName("sec_unit_task_detail_item")
public class UnitTaskDetailItem extends ProgDetailItem {

    private static final long serialVersionUID = 1L;

    @Column(comment = "明细标值id")
    @Schema(description = "明细标值id")
    private Long unitTaskProjectDetailId;

    @Column(comment = "关联方案核算指标明细大项id")
    @Schema(description = "关联方案核算指标明细大项id")
    private Long progDetailItemId;

    @Column(comment = "工号")
    @Schema(description = "工号")
    private String empCode;

    @Column(comment = "分数",decimalLength = 6,length = 15)
    @Schema(description = "分数")
    private BigDecimal point = BigDecimal.ZERO;

    @Column(comment = "金额",decimalLength = 6,length = 15)
    @Schema(description = "金额")
    private BigDecimal amt = BigDecimal.ZERO;

    @TableField(exist = false)
    List<UnitTaskDetailItem> unitTaskDetailItemList = new ArrayList<>();


    //冗余
    @Column(comment = "核算指标id")
    @Schema(description = "核算指标id")
    private Long unitTaskProjectId;

}
