package com.hscloud.hs.cost.account.model.entity.kpi;

import com.baomidou.mybatisplus.annotation.*;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.annotation.IsAutoIncrement;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.pig4cloud.pigx.common.core.util.TenantTable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TenantTable
@Schema(description = "当量调整记录")
@TableName("kpi_item_equivalent_change_record")
public class KpiItemEquivalentChangeRecord extends Model<KpiItemEquivalentChangeRecord> {
    @IsAutoIncrement
    @TableId(value = "id", type = IdType.AUTO)
    @Column(comment = "", type = MySqlTypeConstant.BIGINT)
    private Long id;

    @TableField(value = "task_id")
    @Column(comment = "核验任务id", type = MySqlTypeConstant.BIGINT, isNull = false, defaultValue = "0")
    private Long taskId;

    @TableField(value = "equivalent_id")
    @Column(comment = "当量id", type = MySqlTypeConstant.BIGINT, isNull = false)
    private Long equivalentId;

    @TableField(value = "before_value")
    @Column(comment = "调整前的值", type = MySqlTypeConstant.DECIMAL, length = 24, decimalLength = 6)
    private BigDecimal beforeValue;

    @TableField(value = "operators")
    @Column(comment = "操作符", type = MySqlTypeConstant.VARCHAR)
    private String operators;

    @TableField(value = "change_value")
    @Column(comment = "调整值", type = MySqlTypeConstant.DECIMAL, length = 24, decimalLength = 6)
    private BigDecimal changeValue;

    @TableField(value = "reason")
    @Column(comment = "调整原因", type = MySqlTypeConstant.VARCHAR)
    private String reason;

    @TableField(value = "file_name")
    @Column(comment = "文件名", type = MySqlTypeConstant.VARCHAR)
    private String fileName;

    @TableField(value = "file_url")
    @Column(comment = "文件地址", type = MySqlTypeConstant.VARCHAR)
    private String fileUrl;

    @TableField(value = "status")
    @Column(comment = "-1:驳回 0:未提交 10:待审核 20:通过", type = MySqlTypeConstant.CHAR, length = 2, defaultValue = "0")
    private String status;

    @TableField(value = "change_flag")
    @Column(comment = "修改类型 0-科室 1-绩效办", type = MySqlTypeConstant.CHAR, length = 1)
    private String changeFlag;

    @TableField(value = "change_user_id")
    @Column(comment = "修改人", type = MySqlTypeConstant.BIGINT)
    private Long changeUserId;

    @TableField(value = "group_uuid")
    @Column(comment = "组别uuid", type = MySqlTypeConstant.VARCHAR)
    private String groupUuid;

    @TableField(value = "p_equivalent_id")
    @Column(comment = "科室当量id", type = MySqlTypeConstant.BIGINT)
    private Long pEquivalentId;

    @TableField(value = "distribute_type")
    @Column(comment = "分配方式 0-平均分配，1-系数分配，2-自定义分配")
    private String distributeType;

    @TableField(value = "coefficient")
    @Column(comment = "系数")
    private BigDecimal coefficient;

    @TableField(value = "created_id", fill = FieldFill.INSERT)
    @Column(comment = "创建人", type = MySqlTypeConstant.BIGINT)
    private Long createdId;

    @TableField(value = "created_date", fill = FieldFill.INSERT)
    @Column(comment = "创建时间", type = MySqlTypeConstant.DATETIME)
    private Date createdDate;

    @TableField(value = "updated_id", fill = FieldFill.INSERT_UPDATE)
    @Column(comment = "更新人", type = MySqlTypeConstant.BIGINT)
    private Long updatedId;

    @TableField(value = "updated_date", fill = FieldFill.INSERT_UPDATE)
    @Column(comment = "更新时间", type = MySqlTypeConstant.DATETIME)
    private Date updatedDate;

    @TableField(value = "tenant_id")
    @Column(comment = "租户号", type = MySqlTypeConstant.BIGINT)
    private Long tenantId;
}