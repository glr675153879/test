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
 * @date 2023/11/9 9:03
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema( description="指标校验执行结果")
public class CostVerificationResultIndexNew extends Model<CostVerificationResultIndexNew> {

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
     * 核算指标id
     */
    @Schema(description = "核算指标id")
    private Long indexId;
    /**
     * 核算指标名称
     */
    @Schema(description = "核算指标名称")
    private String indexName;
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
     * 核算项名称
     */
    @Schema(description = "核算项名称")
    private String itemName;
    /**
     * 核算项总值
     */
    @Schema(description = "核算项值")
    private BigDecimal itemCount;
    /**
     * 父级核算指标id
     */
    @Schema(description = "父级核算指标id")
    private Long parentId;
    /**
     * 核算日期
     */
    @Schema(description = "核算日期")
    private String accountDate;
    /**
     * 最外层指标id
     */
    @Schema(description = "最外层指标id")
    private Long outerMostIndexId;
}
