package com.hscloud.hs.cost.account.model.dto.kpi;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.annotation.IsAutoIncrement;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class KpiAccountTaskAddDto {

    @TableId(value = "id", type = IdType.AUTO)
    @IsAutoIncrement
    @Schema(description = "主键")
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

}
