package com.hscloud.hs.cost.account.model.entity.kpi;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.annotation.IsAutoIncrement;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.pig4cloud.pigx.common.core.util.TenantTable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import java.math.BigDecimal;

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
public class KpiIndexFormula extends Model<KpiIndexFormula>{

    @TableId(value = "id", type = IdType.ASSIGN_ID)
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
    private String formula;

    @TableField(value = "show_flag")
    @Column(comment="是否下转展示", type = MySqlTypeConstant.CHAR, length = 1 )
    private String showFlag;

    @TableField(value = "check_flag")
    @Column(comment="是否校验", type = MySqlTypeConstant.CHAR, length = 1 )
    private String checkFlag;

    @TableField(value = "created_id")
    @Column(comment="创建人", type = MySqlTypeConstant.BIGINT)
    private Long createdId;

    @TableField(value = "created_date")
    @Column(comment="创建时间", type = MySqlTypeConstant.DATETIME)
    private Date createdDate;

    @TableField(value = "updated_id")
    @Column(comment="更新人", type = MySqlTypeConstant.BIGINT)
    private Long updatedId;

    @TableField(value = "updated_date")
    @Column(comment="更新时间", type = MySqlTypeConstant.DATETIME)
    private Date updatedDate;

    @TableField(value = "tenant_id")
    @Column(comment="租户号", type = MySqlTypeConstant.BIGINT)
    private Long tenantId;

    @TableField(value = "member_codes")
    @Column(comment="指标，分摊，指标项合集，json存储", type = MySqlTypeConstant.TEXT )
    private String memberCodes;

    @TableField(value = "member_ids")
    @Column(comment="人/科室编码", type = MySqlTypeConstant.TEXT )
    private String memberIds;

    @TableField(value = "formula_group")
    @Column(comment="公式分组", type = MySqlTypeConstant.INT)
    private Integer formulaGroup;

    @TableField(value = "del_flag")
    @Column(comment="是否删除", type = MySqlTypeConstant.CHAR, length = 1 )
    private String delFlag;



}