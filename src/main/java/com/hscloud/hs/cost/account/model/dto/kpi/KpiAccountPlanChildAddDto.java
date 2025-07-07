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
* 核算子方案Model
* @author you
* @since 2024-09-13
*/
@Data
@Accessors(chain = true)
@TenantTable
@Schema(description = "核算子方案表")
@TableName("kpi_account_plan_child")
public class KpiAccountPlanChildAddDto{

    @TableId(value = "id", type = IdType.AUTO)
    @IsAutoIncrement
    @Column(comment="", type = MySqlTypeConstant.BIGINT , isNull = false )
    private Long id;

    @TableField(value = "子方案名称")
    @Column(comment="子方案编码", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String planName;

    @TableField(value = "plan_code")
    @Column(comment="上层方案code", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String planCode;

    @TableField(value = "index_code")
    @Column(comment="指标编码", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String indexCode;

    @TableField(value = "object")
    @Column(comment="方案对象 1人员2科室", type = MySqlTypeConstant.CHAR, length = 1 )
    private String object;

    @TableField(value = "emp_id")
    @Column(comment="人员编码", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String empId;

    @TableField(value = "user_id")
    @Column(comment="人员id(系统)", type = MySqlTypeConstant.BIGINT)
    private Long userId;

    @TableField(value = "dept_id")
    @Column(comment="科室编码", type = MySqlTypeConstant.BIGINT)
    private Long deptId;

    @TableField(value = "status")
    @Column(comment="0启用，1停用，-1草稿", type = MySqlTypeConstant.INT , isNull = false )
    private Integer status;

}