package com.hscloud.hs.cost.account.model.dto.kpi;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.annotation.IsAutoIncrement;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import com.pig4cloud.pigx.common.core.util.TenantTable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
* 核算方案(COST_ACCOUNT_PLAN)Model
* @author you
* @since 2024-09-13
*/

@Data
@Accessors(chain = true)
@Schema(description = "核算方案表(COST_ACCOUNT_PLAN)")
public class KpiAccountPlanAddDto{

    @TableId(value = "id", type = IdType.AUTO)
    @IsAutoIncrement
    @Schema(description = "主键")
    private Long id;

    @TableField(value = "category_code")
    @Schema(description="分组code")
    private String categoryCode;

    @TableField(value = "plan_name")
    @Schema(description="方案名称")
    private String planName;

    @Schema(description = "核算指标")
    private String indexCode;

    @TableField(value = "`range`")
    @Schema(description = "数据范围JSON 公式里fieldList那个对象")
    @Column(comment="数据范围JSON 公式里fieldList那个对象", type = MySqlTypeConstant.TEXT)
    private String range;

    @TableField(value = "status")
    @Schema(description="状态：0：启用  1:停用")
    private String status = "0";

}