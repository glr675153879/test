package com.hscloud.hs.cost.account.model.dto.kpi;

import cn.hutool.core.lang.Dict;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountUnit;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiCategory;
import com.pig4cloud.pigx.admin.api.entity.SysDictItem;
import com.pig4cloud.pigx.admin.api.entity.SysUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "指标适用对象")
public class KpiIndexPlanMemberDto {
    private String type;
    private String value;
    private String label;
    @TableField(value = "status")
    @Schema(description="启停用标记，0启用，1停用")
    private String status;

    @TableField(value = "del_flag",fill = FieldFill.INSERT)
    @TableLogic(value = "0", delval = "1")
    @Schema(description="删除标记，0未删除，1已删除")
    private String delFlag;

    public static KpiIndexPlanMemberDto convertByAccountUnit(KpiAccountUnit t){
        KpiIndexPlanMemberDto kpiIndexPlanMemberDto = new KpiIndexPlanMemberDto();
        kpiIndexPlanMemberDto.setType("normal");
        kpiIndexPlanMemberDto.setValue(t.getId().toString());
        kpiIndexPlanMemberDto.setLabel(t.getName());
        kpiIndexPlanMemberDto.setDelFlag(t.getDelFlag());
        kpiIndexPlanMemberDto.setStatus(t.getStatus());
        return kpiIndexPlanMemberDto;
    }

    public static KpiIndexPlanMemberDto convertBySysUser(SysUser t){
        KpiIndexPlanMemberDto kpiIndexPlanMemberDto = new KpiIndexPlanMemberDto();
        kpiIndexPlanMemberDto.setType("normal");
        kpiIndexPlanMemberDto.setValue(t.getUserId().toString());
        kpiIndexPlanMemberDto.setLabel(t.getName());
        kpiIndexPlanMemberDto.setDelFlag(t.getDelFlag());
        kpiIndexPlanMemberDto.setStatus(t.getStatus());
        return kpiIndexPlanMemberDto;
    }

    public static KpiIndexPlanMemberDto convertByCategory(KpiCategory kpiCategory) {
        KpiIndexPlanMemberDto kpiIndexPlanMemberDto = new KpiIndexPlanMemberDto();
        kpiIndexPlanMemberDto.setType("group");
        kpiIndexPlanMemberDto.setValue(kpiCategory.getCategoryCode());
        kpiIndexPlanMemberDto.setLabel(kpiCategory.getCategoryName());
        return kpiIndexPlanMemberDto;
    }

    public static KpiIndexPlanMemberDto convertByDict(SysDictItem sysDictItem) {
        KpiIndexPlanMemberDto kpiIndexPlanMemberDto = new KpiIndexPlanMemberDto();
        kpiIndexPlanMemberDto.setType("accountType");
        kpiIndexPlanMemberDto.setValue(sysDictItem.getItemValue());
        kpiIndexPlanMemberDto.setLabel(sysDictItem.getLabel());
        return kpiIndexPlanMemberDto;
    }
}
