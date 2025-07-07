package com.hscloud.hs.cost.account.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * @author 小小w
 * @date 2023/11/7 15:00
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema( description="定时任务核算项执行结果")
public class CostVerificationResultItem extends Model<CostVerificationResultItem> {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;
    /**
     * 核算单元id
     */
    @Schema(description = "核算单元id")
    private Long unitId;
    /**
     * 核算单元名称
     */
    @Schema(description = "核算单元名称")
    private String unitName;
    /**
     * 核算项id
     */
    @Schema(description = "核算项id")
    private Long itemId;
    /**
     * 核算项名称
     */
    @Schema(description = "核算项名称")
    private String itemName;
    /**
     * 核算时间段
     */
    @Schema(description = "核算时间段")
    private String accountDate;
    /**
     * 核算项值
     */
    @Schema(description = "核算项值")
    private BigDecimal itemCount;
    /**
     * 核算维度
     */
    @Schema(description = "核算维度")
    private String dimension;
    /**
     * 核算对象类型
     */
    @Schema(description = "核算对象类型")
    private String type;
    /**
     * 核算对象id
     */
    @Schema(description = "核算对象id")
    private Long objectId;
    /**
     * 核算对象值
     */
    @Schema(description = "核算对象值")
    private BigDecimal objectCount;

}
