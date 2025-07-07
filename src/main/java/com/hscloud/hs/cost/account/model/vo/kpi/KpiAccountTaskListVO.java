package com.hscloud.hs.cost.account.model.vo.kpi;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.annotation.*;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.annotation.IsAutoIncrement;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountPlan;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountTask;
import com.pig4cloud.pigx.common.core.util.TenantTable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
@TenantTable
@Schema(description = "核算方案表(COST_ACCOUNT_PLAN)")
@TableName("kpi_account_plan")
public class KpiAccountTaskListVO {

    @TableId(value = "id", type = IdType.AUTO)
    @IsAutoIncrement
    @Column(comment="", type = MySqlTypeConstant.BIGINT , isNull = false )
    private Long id;

    @TableField(value = "account_task_name")
    @Column(comment="核算任务名称", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String accountTaskName;

    @TableField(value = "period")
    @Column(comment="核算周期", type = MySqlTypeConstant.INT , isNull = false )
    private Long period;

    @TableField(value = "plan_code")
    @Column(comment="核算方案", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String planCode;

    @TableField(value = "status")
    @Column(comment="状态", type = MySqlTypeConstant.INT , isNull = false )
    private Long status;

    @TableField(value = "del_flag")
    @Column(comment="是否删除：0：未删除 1：删除", type = MySqlTypeConstant.CHAR, length = 1 )
    private String delFlag;

    @TableField(value = "issued_flag")
    @Column(comment="是否锁定 Y/N", type = MySqlTypeConstant.CHAR, length = 1 )
    private String issuedFlag;

    @TableField(value = "issued_date",updateStrategy = FieldStrategy.IGNORED)
    @Column(comment="锁定时间", type = MySqlTypeConstant.DATETIME)
    private Date issuedDate;

    @TableField(value = "send_flag")
    @Column(comment="是否下发 Y/N", type = MySqlTypeConstant.CHAR, length = 1 )
    private String sendFlag;

    @TableField(value = "send_date")
    @Column(comment="下发时间", type = MySqlTypeConstant.DATETIME)
    private Date sendDate;

/*    @TableField(value = "send_log",updateStrategy = FieldStrategy.NEVER)
    @Column(comment="下发错误日志", type = MySqlTypeConstant.TEXT )
    private String sendLog;*/

    @Column(comment="计算时间", type = MySqlTypeConstant.DATETIME)
    private Date updatedDate;


    @TableField(value = "task_child_id")
    @Column(comment="最后一次子任务ID", type = MySqlTypeConstant.BIGINT)
    private Long taskChildId;

    private Long childStatus;

    private String statusName;
    private String planName;

    private String indexName;
    private String indexCode;

    private Long reportId;

    public static KpiAccountTaskListVO convertByKpiAccountTask(KpiAccountTask t){
        return BeanUtil.copyProperties(t, KpiAccountTaskListVO.class);
    }
}
