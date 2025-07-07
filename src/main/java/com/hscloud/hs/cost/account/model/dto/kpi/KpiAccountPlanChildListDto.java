package com.hscloud.hs.cost.account.model.dto.kpi;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.annotation.IsAutoIncrement;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import com.hscloud.hs.cost.account.model.dto.PageDto;
import com.pig4cloud.pigx.common.core.util.TenantTable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
* 核算子方案Model
* @author you
* @since 2024-09-13
*/

@Data
@Accessors(chain = true)
@TenantTable
@Schema(description = "核算子方案表")
@TableName("kpi_account_plan_child")
public class KpiAccountPlanChildListDto extends PageDto {

    @Schema(description = "总方案code")
    private String planCode;

    @Schema(description = "子方案id")
    private Long id;

    @Schema(description = "方案名称")
    private String planName;

    @Schema(description="创建时间0")
    private Date createdDate0;

    @Schema(description="创建时间1")
    private Date createdDate1;

    private String status;

    public Date getCreatedDate0() {
        return createdDate0 == null?null: DateUtil.beginOfDay(createdDate0);
    }

    public Date getCreatedDate1() {
        return createdDate1 == null?null: DateUtil.endOfDay(createdDate1);
    }
}