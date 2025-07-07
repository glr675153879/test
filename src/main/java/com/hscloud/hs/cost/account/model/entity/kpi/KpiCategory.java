package com.hscloud.hs.cost.account.model.entity.kpi;

import com.baomidou.mybatisplus.annotation.*;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.annotation.Index;
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
 * 分组Model
 *
 * @author you
 * @since 2024-09-13
 */

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TenantTable
@Schema(description = "分组表")
@TableName("kpi_category")
public class KpiCategory extends Model<KpiCategory> {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Column(comment = "", type = MySqlTypeConstant.BIGINT, isNull = false)
    private Long id;

    @TableField(value = "category_type")
    @Column(comment = "分类类型", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String categoryType;

    @TableField(value = "category_code")
    @Column(comment = "分类代码", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String categoryCode;

    @TableField(value = "third_code", updateStrategy = FieldStrategy.ALWAYS)
    @Column(comment = "三方编码", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String thirdCode;

    @TableField(value = "category_name")
    @Column(comment = "分类名称", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String categoryName;

    @TableField(value = "description")
    @Column(comment = "描述", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String description;

    @TableField(value = "parent_id")
    @Column(comment = "父亲节点ID", type = MySqlTypeConstant.BIGINT, isNull = false)
    private Long parentId;

    @TableField(value = "created_id")
    @Column(comment = "创建用户ID", type = MySqlTypeConstant.BIGINT)
    private Long createdId;

    @TableField(value = "created_date")
    @Column(comment = "", type = MySqlTypeConstant.DATETIME)
    private Date createdDate;

    @TableField(value = "seq")
    @Column(comment = "排序号", type = MySqlTypeConstant.INT)
    private Long seq;

    @TableField(value = "status")
    @Column(comment = "状态;0启用 1停用", type = MySqlTypeConstant.INT)
    private Long status;

    @TableField(value = "del_flag")
    @Column(comment = "删除  0未删 1已删", type = MySqlTypeConstant.CHAR, length = 1, isNull = false, defaultValue = "0")
    private String delFlag;

    @TableField(value = "tenant_id")
    @Column(comment = "", type = MySqlTypeConstant.BIGINT)
    private Long tenantId;

    @TableField(value = "plan_type")
    @Column(comment = "方案分组", type = MySqlTypeConstant.VARCHAR, length = 100)
    private String planType;

    @TableField(value = "busi_type")
    @Column(comment = "业务类型，1，一次绩效，2，科室成本", isNull = false, defaultValue = "1", type = MySqlTypeConstant.VARCHAR, length = 20)
    @Index
    private String busiType= "1";
}