package com.hscloud.hs.cost.account.model.entity.dataReport;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import com.pig4cloud.pigx.common.core.util.TenantTable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "我的上报附件表")
@Entity
@TenantTable
@Table(name = "cost_report_record_file_info")
public class CostReportRecordFileInfo extends Model<CostReportRecordFileInfo> {
    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "我的上报id")
    private Long recordId;

    @Schema(description = "文件名称")
    private String fileName;

    @Schema(description = "地址")
    private String url;

    @Schema(description = "尺寸")
    private Integer size;

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

}
