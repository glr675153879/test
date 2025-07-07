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
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountPlanChild;
import com.pig4cloud.pigx.common.core.util.TenantTable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
* 核算子方案Model
* @author you
* @since 2024-09-13
*/

@Data
@Accessors(chain = true)
@TenantTable
@Schema(description = "核算子方案表")
@TableName("kpi_account_plan_child")
public class KpiAccountPlanChildListVO{
    private Long id;

    @TableField(value = "子方案名称")
    @Schema(description="子方案名称")
    private String planName;

    @TableField(value = "object")
    @Schema(description="方案对象 1人员2科室")
    private String object;

    @Schema(description = "更新人姓名")
    private String updateName;

    @Schema(description="更新人id")
    private Long updatedId;

    @TableField(value = "status")
    @Schema(description="0启用，1停用，-1草稿")
    private Integer status;

    private Long memberCode;

    private String memberName;

    private Long userId;

    private Long deptId;

    private String indexCode;

    private String indexName;

    public static KpiAccountPlanChildListVO convertByKpiAccountPlanChildList(KpiAccountPlanChild t){
        return BeanUtil.copyProperties(t, KpiAccountPlanChildListVO.class);
    }
}