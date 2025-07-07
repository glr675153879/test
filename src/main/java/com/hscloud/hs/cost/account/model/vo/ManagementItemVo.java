package com.hscloud.hs.cost.account.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理
 * @author banana
 * @create 2023-11-24 14:27
 */
@Data
public class ManagementItemVo {

    @Schema(description = "主键id")
    private Long id;

    @Schema(description = "管理岗位")
    private String position;

    @Schema(description = "管理绩效金额")
    private String amount;

    @Schema(description = "计算单位")
    private String unit;
}
