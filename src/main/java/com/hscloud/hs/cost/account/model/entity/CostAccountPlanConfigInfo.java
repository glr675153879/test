package com.hscloud.hs.cost.account.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CostAccountPlanConfigInfo extends Model<CostAccountPlanConfigInfo> {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private Long id;


    /**
     * 方案配置id
     */
    @Schema(description = "方案配置id")
    private Long configId;
    /**
     * pid 父节点0
     */
    @Schema(description = "pid 父节点0")
    private Long parentIndexId;
    /**
     * 项或者指标id
     */
    @Schema(description = "项或者指标id")
    private Long indexId;

    /**
     * 配置指标项id
     */
    @Schema(description = "配置指标项id")
    private Long configIndexId;
}
