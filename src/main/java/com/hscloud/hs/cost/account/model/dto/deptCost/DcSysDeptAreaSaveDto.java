package com.hscloud.hs.cost.account.model.dto.deptCost;

import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

/**
 * @author banana
 * @create 2024-09-23 14:10
 */
@Data
public class DcSysDeptAreaSaveDto {

    @Schema(description = "主键id")
    private Long id;

    @Schema(description = "周期")
    @NotBlank(message = "周期不能为空")
    private String cycle;

    @Schema(description = "核算单元名称")
    @NotBlank(message = "核算单元名称不能为空")
    private String accountUnitName;

    @Schema(description = "核算单元id")
    @NotBlank(message = "核算单元id不能为空")
    private String accountUnitId;

    @Schema(description = "合集")
    private BigDecimal sum;

    @Schema(description = "门诊楼")
    private BigDecimal menZhenLou;

    @Schema(description = "住院楼")
    private BigDecimal zhuYuanLou;

    @Schema(description = "新综合楼")
    private BigDecimal xinZongHeLou;

    @Schema(description = "文化站")
    private BigDecimal wenHuaZhan;

    @Schema(description = "理疗科楼")
    private BigDecimal liLiaoKeLou;

    @Schema(description = "老办公楼")
    private BigDecimal laoBanGongLou;

    @Schema(description = "康复医学楼")
    private BigDecimal kangFuYiXueLou;

    @Schema(description = "矿服楼")
    private BigDecimal kuangFuLou;

}
