package com.hscloud.hs.cost.account.model.dto.kpi;

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
* 指标Model
* @author you
* @since 2024-09-13
*/

@Data
@Accessors(chain = true)
@TenantTable
@Schema(description = "指标表列表查询入参")
public class KpiIndexListDto extends PageDto {

    @Schema(description = "指标分组")
    private String categoryCode;

    @TableField(value = "name")
    @Column(comment="核算指标名称", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String name;

    @Schema(description = "指标类型 1非条件指标 2条件指标3分摊指标 10.条件+非条件 11.分摊")
    private String type;

    private String status;

    @Schema(description = "口径颗粒度 1人2科室3归集4固定值")
    private String caliber;

    private String codes;

    private String impFlag;

    private String secondFlag;

    @Schema(description = "归集规则")
    private String impCategoryCode;
}