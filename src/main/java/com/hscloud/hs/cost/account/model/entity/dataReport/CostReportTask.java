package com.hscloud.hs.cost.account.model.entity.dataReport;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import com.hscloud.hs.cost.account.model.dto.dataReport.CostReportItemDto;
import com.hscloud.hs.cost.account.model.dto.dataReport.SysDeptDto;
import com.hscloud.hs.cost.account.model.dto.dataReport.SysUserDto;
import com.pig4cloud.pigx.common.core.util.TenantTable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "上报任务")
@Entity
@TenantTable
@Table(name = "cost_report_task")
public class CostReportTask extends Model<CostReportTask> {
    private static final long serialVersionUID = 1L;

    @Schema(description = "上报任务id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "任务名称")
    private String taskName;

    @Schema(description = "上报项")
    private String itemList;

    @Schema(description = "上报方式")
    private String measure;

    @Column(comment = "是否行继承 1是 0否", defaultValue = "0")
    @Schema(description = "是否行继承 1是 0否")
    private String ifInherit;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "上报人")
    private String userList;

    @Schema(description = "口径颗粒度 1全院 2核算单元 3核算单元+人员 4人员 5RW(作废) 6固定值（科室成本） 7核算单元（科室成本） ")
    private String reportType;

    @Column(comment = "上报频率 字典")
    @Schema(description = "上报频率 字典")
    private String frequencyType;

    @Schema(description = "任务生成时间")
    private String startTime;

    @Schema(description = "任务截止时间")
    private String endTime;

    @Schema(description = "核算周期 yyyy-MM")
    private String calculateCircle;

    @Schema(description = "上报科室")
    private String reportDeptList;

    @Schema(description = "启停用标记，0启用，1停用")
    private String status;

    @Schema(description = "初始化状态，Y已初始化，N未初始化")
    private String initialized;

    @Column(comment = "业务类型: 0绩效 1科室成本", length = 1,
            type = MySqlTypeConstant.VARCHAR, defaultValue = "0")
    @Schema(description = "业务类型: 0绩效 1科室成本")
    private String type = "0";

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建人")
    private String createBy;

    /**
     * 修改人
     */
    @TableField(fill = FieldFill.UPDATE)
    @Schema(description = "修改人")
    private String updateBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.UPDATE)
    @Schema(description = "修改时间")
    private LocalDateTime updateTime;

    /**
     * 0-正常，1-删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "删除标记,1:已删除,0:正常")
    private String delFlag;

    /**
     * 租户ID
     */
    @Schema(description = "租户ID")
    private Long tenantId;

    @TableField(exist = false)
    @Schema(description = "上报项列表")
    private List<CostReportItemDto> itemVoList;

    @TableField(exist = false)
    @Schema(description = "上报人列表")
    private SysUserDto userVoList;

    @TableField(exist = false)
    @Schema(description = "上报科室列表")
    private SysDeptDto deptVoList;


    public List<CostReportItemDto> queryItemVoList() {
        List<CostReportItemDto> costReportItemDtos = JSON.parseArray(itemList, CostReportItemDto.class);
        return itemVoList = (costReportItemDtos == null ? new ArrayList<>() : costReportItemDtos);
    }

}
