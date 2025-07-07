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
 * <p>
 * 指标校验执行结果
 * </p>
 *
 * @author
 * @since 2023-10-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema( description="指标校验执行结果")
public class CostVerificationResultIndex extends Model<CostVerificationResultIndex> {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;
    /**
     * 核算单元id
     */
    @Schema(description = "核算单元id")
    private Long unitId;
    /**
     * 核算指标id
     */
    @Schema(description = "核算指标id")
    private Long indexId;
    /**
     * 核算指标key
     */
    @Schema(description = "核算指标key")
    private String indexKey;
    /**
     * 核算指标计算的公式
     */
    @Schema(description = "核算指标计算的公式")
    private String calculateFormulaDesc;
    /**
     * 指标核算值
     */
    @Schema(description = "指标核算值")
    private BigDecimal indexCount;
    /**
     * 核算项id
     */
    @Schema(description = "核算项id")
    private Long itemId;
    /**
     * 核算项key
     */
    @Schema(description = "核算项key")
    private String itemKey;
    /**
     * 核算项总值
     */
    @Schema(description = "核算项总值")
    private BigDecimal itemCount;
    /**
     * 父级核算指标id
     */
    @Schema(description = "父级核算指标id")
    private Long parentId;
    /**
     * 路径
     */
    @Schema(description = "路径")
    private String path;
    /**
     * 核算维度
     */
    @Schema(description = "核算维度")
    private String type;
    /**
     * 核算对象id
     */
    @Schema(description = "核算对象id")
    private Long objectId;
    /**
     * 核算对象核算值
     */
    @Schema(description = "核算对象核算值")
    private BigDecimal objectResult;
    /**
     * 核算日期
     */
    @Schema(description = "核算日期")
    private String accountDate;
    /**
     * 最外层指标id
     */
    @Schema(description = "最外层指标id")
    private Long outerMostIndexId ;
    /**
     * 租户id
     */
    @Schema(description = "租户id")
    private Long tenantId;
}
