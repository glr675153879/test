package com.hscloud.hs.cost.account.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.hscloud.hs.cost.account.constant.enums.BizTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @author Admin
 */
@Data
@Schema(name = "CostDataChangeRecord", description = "数据变更记录")
@EqualsAndHashCode(callSuper = true)
public class CostDataChangeRecord extends Model<CostDataChangeRecord> {


    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(type = IdType.AUTO)
    private Integer id;

    @Schema(description = "业务id")
    private Long bizId;

    @Schema(description = "业务类型")
    private BizTypeEnum bizCode;

    @Schema(description = "变更项")
    private String changeItem;

    @Schema(description = "变更来源")
    private String changeSource;

    @Schema(description = "变更类型")
    private String changeType;

    @Schema(description = "变更前")
    private String changeBefore;

    @Schema(description = "规则")
    private String rule;

    @Schema(description = "生效时间")
    private LocalDateTime effectTime;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "变更后")
    private String changeAfter;

    @Schema(description = "变更描述")
    private String changeDesc;

    @Schema(description = "变更时间")
    private LocalDateTime changeTime;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private String createBy;

    /**
     * 修改人
     */

    @Schema(description = "修改人")
    private String updateBy;

    /**
     * 创建时间
     */

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @Schema(description = "修改时间")
    private LocalDateTime updateTime;

    /**
     * 租户ID
     */
    @Schema(description = "租户ID")
    private Long tenantId;

}
