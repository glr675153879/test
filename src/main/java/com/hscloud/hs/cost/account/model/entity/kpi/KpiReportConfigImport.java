package com.hscloud.hs.cost.account.model.entity.kpi;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import com.pig4cloud.pigx.common.core.util.TenantTable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 一次报表数据导入;
 * @author : http://www.chiner.pro
 * @date : 2024-12-11
 */
@Data
@Accessors(chain = true)
@TenantTable
@Schema(description = "一次报表数据导入")
@TableName("kpi_report_config_import")
public class KpiReportConfigImport extends Model<KpiReportConfigImport>{
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @TableField("ID")
    private Long id ;
    @TableField("TASK_CHILD_ID")
    @Column(comment="", type = MySqlTypeConstant.BIGINT)
    private Long taskChildId ;
    @TableField("REPORT_ID")
    @Column(comment="", type = MySqlTypeConstant.BIGINT)
    private Long reportId ;
    @TableField("USER_ID")
    @Column(comment="", type = MySqlTypeConstant.BIGINT)
    private Long userId ;
    @TableField("DEPT_ID")
    @Column(comment="", type = MySqlTypeConstant.BIGINT)
    private Long deptId ;
    @TableField("VALUE")
    @Column(comment="对比值",type = MySqlTypeConstant.DECIMAL, length = 24, decimalLength = 6)
    private BigDecimal value ;
    @TableField("CODE")
    @Column(comment="",  type = MySqlTypeConstant.VARCHAR, length = 255)
    private String code ;
    @TableField("CREATE_DATE")
    @Column(comment="", type = MySqlTypeConstant.DATETIME)
    private Date createDate ;
    @TableField("USER_NAME")
    @Column(comment="",  type = MySqlTypeConstant.VARCHAR, length = 255)
    private String userName ;
    @TableField("DEPT_NAME")
    @Column(comment="",  type = MySqlTypeConstant.VARCHAR, length = 255)
    private String deptName ;
    @TableField("NAME")
    @Column(comment="",  type = MySqlTypeConstant.VARCHAR, length = 255)
    private String name ;
    @TableField("TENANT_ID")
    @Column(comment="", type = MySqlTypeConstant.BIGINT)
    private Long tenantId ;

    @TableField("PERIOD")
    @Column(comment="", type = MySqlTypeConstant.BIGINT)
    private Long period ;

    @TableField(exist = false)
    private String outName ;
    @TableField(exist = false)
    private Map<Long,BigDecimal> map = new HashMap<>();

}