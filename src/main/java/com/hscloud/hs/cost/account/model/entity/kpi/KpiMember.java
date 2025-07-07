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
@TableName("kpi_member")
public class KpiMember extends Model<KpiMember>{

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
    @Column(comment="", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String hostCode;

    @TableField(value = "member_id")
    @Column(comment="", type = MySqlTypeConstant.BIGINT)
    private Long memberId;

    @TableField(value = "member_code")
    @Column(comment="", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String memberCode;

    @TableField(value = "member_type")
    @Column(comment="", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String memberType;

    @TableField(value = "created_date")
    @Column(comment="", type = MySqlTypeConstant.DATETIME)
    private Date createdDate;

    @TableField(value = "tenant_id")
    @Column(comment="", type = MySqlTypeConstant.BIGINT)
    private Long tenantId;

    @TableField(value = "busi_type")
    @Column(comment = "业务类型，1，一次绩效，2，科室成本", isNull = false, defaultValue = "1", type = MySqlTypeConstant.VARCHAR, length = 20)
    @Index
    private String busiType= "1";
}