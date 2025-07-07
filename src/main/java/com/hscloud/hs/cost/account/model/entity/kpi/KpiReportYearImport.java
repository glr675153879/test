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
import java.util.Date;

/**
 * 年度报表导入;
 * @author : http://www.chiner.pro
 * @date : 2025-1-8
 */
@Data
@Accessors(chain = true)
@TenantTable
@Schema(description = "年度报表导入")
@TableName("kpi_report_year_import")
public class KpiReportYearImport extends Model<KpiReportYearImport>{
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @TableField("ID")
    @Column(comment="", type = MySqlTypeConstant.BIGINT , isNull = false )
    private Long id ;
    @TableField("TASK_CHILD_ID")
    @Column(comment="", type = MySqlTypeConstant.BIGINT  )
    private Long taskChildId ;

    @TableField("REPORT_ID")
    @Column(comment="", type = MySqlTypeConstant.BIGINT  )
    private Long reportId ;

    @TableField("USER_ID")
    @Column(comment="", type = MySqlTypeConstant.BIGINT  )
    private Long userId ;

    @TableField("DEPT_ID")
    @Column(comment="", type = MySqlTypeConstant.BIGINT  )
    private Long deptId ;

    @TableField("CREATE_DATE")
    @Column(comment="", type = MySqlTypeConstant.DATETIME)
    private Date createDate ;

    @TableField("user_name")
    @Column(comment="", type = MySqlTypeConstant.VARCHAR, length = 255  )
    private String userName ;

    @TableField("dept_name")
    @Column(comment="", type = MySqlTypeConstant.VARCHAR, length = 255  )
    private String deptName ;

    @TableField("tenant_id")
    @Column(comment="", type = MySqlTypeConstant.BIGINT  )
    private Long tenantId ;

    @TableField("period")
    @Column(comment="", type = MySqlTypeConstant.BIGINT  )
    private Long period ;

    @TableField("JSON")
    @Column(comment="", type = MySqlTypeConstant.TEXT  )
    private String json ;

    @Column(comment="", type = MySqlTypeConstant.VARCHAR, length = 255  )
    @TableField("EMP_CODE")
    private String empCode;
}