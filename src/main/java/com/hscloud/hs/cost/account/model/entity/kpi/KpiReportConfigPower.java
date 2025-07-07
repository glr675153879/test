package com.hscloud.hs.cost.account.model.entity.kpi;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.pig4cloud.pigx.common.core.util.TenantTable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import java.math.BigDecimal;

/**
 * 报表权限;
 * @author : http://www.chiner.pro
 * @date : 2024-12-6
 */
@Data
@Accessors(chain = true)
@TenantTable
@Schema(description = "报表权限")
@TableName("kpi_report_config_power")
public class KpiReportConfigPower extends Model<KpiReportConfigPower>{
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Column(comment="", type = MySqlTypeConstant.BIGINT , isNull = false )
    private Long id;
    @TableField("TYPE")
    @Schema(description = "1人2人员分组 3科室 人能看到哪个科室的数据")
    @Column(comment = "1人2人员分组 3科室 人能看到哪个科室的数据",  type = MySqlTypeConstant.VARCHAR, length = 255)
    private String type ;
    @TableField("USER_ID")
    @Schema(description = "人员id")
    @Column(comment = "人员id", type = MySqlTypeConstant.BIGINT)
    private Long userId ;
    @TableField("GROUP_CODE")
    @Schema(description = "分组")
    @Column(comment = "分组",  type = MySqlTypeConstant.VARCHAR, length = 255)
    private String groupCode ;
    @TableField("DEPT_ID")
    @Schema(description = "科室id")
    @Column(comment = "科室id", type = MySqlTypeConstant.BIGINT)
    private Long deptId ;
    @TableField("REPORT_ID")
    @Schema(description = "报表id")
    @Column(comment = "报表id", type = MySqlTypeConstant.BIGINT)
    private Long reportId ;

    @TableField("TENANT_ID")
    @Column(comment = "", type = MySqlTypeConstant.BIGINT)
    private Long tenantId ;

}