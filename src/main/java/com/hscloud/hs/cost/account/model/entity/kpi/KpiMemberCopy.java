package com.hscloud.hs.cost.account.model.entity.kpi;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
* Model
* @author you
* @since 2024-09-13
*/

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TenantTable
@Schema(description = "")
@TableName("kpi_member_copy")
public class KpiMemberCopy extends Model<KpiMemberCopy>{

    @TableId(value = "id", type = IdType.AUTO)
    @IsAutoIncrement
    @Column(comment="", type = MySqlTypeConstant.BIGINT , isNull = false )
    private Long id;

    @TableField(value = "period")
    @Column(comment="周期", type = MySqlTypeConstant.INT , isNull = false )
    private Long period;

    @TableField(value = "host_id")
    @Column(comment="", type = MySqlTypeConstant.BIGINT)
    private Long hostId;

    @TableField(value = "host_code")
    @Column(comment="", type = MySqlTypeConstant.VARCHAR, length = 50)
    private String hostCode;

    @TableField(value = "member_id")
    @Column(comment="", type = MySqlTypeConstant.BIGINT)
    private Long memberId;

    @TableField(value = "member_code")
    @Column(comment="", type = MySqlTypeConstant.VARCHAR, length = 50)
    private String memberCode;

    @TableField(value = "member_type")
    @Column(comment="", type = MySqlTypeConstant.VARCHAR, length = 50)
    private String memberType;

    @TableField(value = "json")
    @Column(comment="内容", type = MySqlTypeConstant.TEXT )
    private String json;

    @TableField(value = "type")
    @Column(comment="类型 人员分组/归集结果/归集单元/医护关系", type = MySqlTypeConstant.INT)
    private Long type;

    @TableField(value = "task_child_id")
    @Column(comment="子任务id", type = MySqlTypeConstant.BIGINT , isNull = false )
    private Long taskChildId;

    @TableField(value = "copy_date")
    @Column(comment="备份时间", type = MySqlTypeConstant.DATETIME , isNull = false )
    private Date copyDate;

    @TableField(value = "tenant_id")
    @Column(comment="", type = MySqlTypeConstant.BIGINT)
    private Long tenantId;

    @TableField(value = "busi_type")
    @Column(comment = "业务类型，1，一次绩效，2，科室成本", isNull = false, defaultValue = "1", type = MySqlTypeConstant.VARCHAR, length = 20)
    @Index
    private String busiType= "1";
}