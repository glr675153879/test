package com.hscloud.hs.cost.account.model.vo.kpi;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.annotation.IsAutoIncrement;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiAccountPlanListDto;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountPlan;
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
@TenantTable
@Schema(description = "核算方案表(COST_ACCOUNT_PLAN)")
@TableName("kpi_account_plan")
public class KpiAccountPlanListVO{

    @TableId(value = "id", type = IdType.AUTO)
    @IsAutoIncrement
    @Column(comment="", type = MySqlTypeConstant.INT , isNull = false )
    private Long id;

    @TableField(value = "category_code")
    @Column(comment="分组code", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String categoryCode;

    @TableField(value = "plan_code")
    @Column(comment="方案code，按规则生成", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String planCode;

    private String planName;

    @Schema(description="指标Code")
    private String indexCode;

    @Schema(description = "指标名称")
    private String indexName;

    @TableField(value = "account_category_code")
    @Schema(description="科室分组")
    private String accountCategoryCode;

    @Schema(description="科室分组名称")
    private String accountCategoryName;

    @TableField(value = "user_category_code")
    @Column(comment="人员分组", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String userCategoryCode;
//
    @Schema(description="人员分组名称")
    private String userCategoryName;

    @TableField(value = "del_flag")
    @Column(comment="是否删除：0：未删除 1：删除", type = MySqlTypeConstant.CHAR, length = 1 )
    private String delFlag;

    @TableField(value = "status")
    @Column(comment="状态：0：启用  1:停用", type = MySqlTypeConstant.CHAR, length = 1 )
    private String status;

    public static KpiAccountPlanListVO convertByKpiAccountPlan(KpiAccountPlan t){
        return BeanUtil.copyProperties(t, KpiAccountPlanListVO.class);
    }

}