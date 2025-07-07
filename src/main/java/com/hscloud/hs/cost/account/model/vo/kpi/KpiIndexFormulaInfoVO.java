package com.hscloud.hs.cost.account.model.vo.kpi;

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
import java.util.List;

/**
* 指标公式Model
* @author you
* @since 2024-09-13
*/

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TenantTable
@Schema(description = "指标公式表")
@TableName("kpi_index_formula")
public class KpiIndexFormulaInfoVO extends Model<KpiIndexFormulaInfoVO>{

    @TableId(value = "id", type = IdType.AUTO)
    @IsAutoIncrement
    @Column(comment="", type = MySqlTypeConstant.BIGINT , isNull = false )
    private Long id;

    @TableField(value = "index_code")
    @Column(comment="指标code", type = MySqlTypeConstant.VARCHAR, length = 255 , isNull = false )
    private String indexCode;

    @TableField(value = "plan_code")
    @Column(comment="方案编码", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String planCode;

    @TableField(value = "formula")
    @Column(comment="公式", type = MySqlTypeConstant.TEXT )
    private List<String> formulas;

    @TableField(value = "member_ids")
    @Column(comment="人/科室编码", type = MySqlTypeConstant.TEXT )
    private String memberIds;


}