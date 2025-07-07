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
* 核算方案(COST_ACCOUNT_PLAN)Model
* @author you
* @since 2024-09-13
*/

@Data
@Accessors(chain = true)
@Schema(description = "核算方案表(COST_ACCOUNT_PLAN)")
public class KpiAccountPlanListDto extends PageDto {

    private String categoryCode;

    private String planName;

    private String planCode;

    private Date createdDate0;

    private Date createDate1;

    public Date getCreatedDate0() {
        return createdDate0==null?null: DateUtil.beginOfDay(createdDate0);
    }

    public Date getCreateDate1() {
        return createDate1==null?null: DateUtil.endOfDay(createDate1);
    }
}