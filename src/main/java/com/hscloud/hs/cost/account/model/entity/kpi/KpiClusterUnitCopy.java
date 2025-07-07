package com.hscloud.hs.cost.account.model.entity.kpi;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.annotation.IsAutoIncrement;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "cost_cluster_unit转存")
@Entity
@Table(name = "kpi_cluster_unit_copy")
public class KpiClusterUnitCopy extends Model<KpiClusterUnitCopy> {
    private static final long serialVersionUID = 1L;

    @TableId(value = "zj", type = IdType.AUTO)
    @IsAutoIncrement
    @Column(comment="", type = MySqlTypeConstant.BIGINT , isNull = false )
    private Long zj;

    @TableField(value = "id")
    @Column(comment="", type = MySqlTypeConstant.BIGINT , isNull = false )
    private Long id;

    @Schema(description = "归集单元名称")
    @Column(comment="归集单元名称", type = MySqlTypeConstant.VARCHAR , length = 255 )
    private String name;

    @Column(comment="分摊科室名称", type = MySqlTypeConstant.TEXT )
    @Schema(description = "分摊科室名称")
    private String units;

    @Column(comment="固定科室单元标识 0 否 1是", type = MySqlTypeConstant.CHAR, length = 1 )
    @Schema(description = "固定科室单元标识 0 否 1是")
    private String isFixUnit;

    @Column(comment="启停用标记，0启用，1停用", type = MySqlTypeConstant.CHAR, length = 1 )
    @Schema(description = "启停用标记，0启用，1停用")
    private String status;

    @Column(comment="初始化状态，Y已初始化，N未初始化", type = MySqlTypeConstant.CHAR, length = 1 )
    @Schema(description = "初始化状态，Y已初始化，N未初始化")
    private String initialized;

    @Column(comment="删除标记,0未删除1已删除", type = MySqlTypeConstant.CHAR, length = 1 )
    @Schema(description = "删除标记,0未删除1已删除")
    private String del_flag;

    @Column(comment="第三方核算单元", type = MySqlTypeConstant.BIGINT )
    @Schema(description = "第三方核算单元")
    private Long thirdAccountId;

    @Column(comment="第三方应用id", type = MySqlTypeConstant.BIGINT )
    @Schema(description = "第三方应用id")
    private Long thirdId;

    @Column(comment="第三方应用名称", type = MySqlTypeConstant.VARCHAR , length = 255 )
    @Schema(description = "第三方应用名称")
    private String thirdName;

    @Column(comment = "新增人",  type = MySqlTypeConstant.VARCHAR,length = 64)
    @Schema(description = "新增人")
    private String createBy;

    @Column(comment = "新增日期", type = MySqlTypeConstant.DATE)
    @Schema(description = "新增日期")
    private Date createTime;

    @Column(comment = "修改人", type = MySqlTypeConstant.VARCHAR,length = 64)
    @Schema(description = "修改人")
    private String updateBy;

    @Column(comment = "修改日期", type = MySqlTypeConstant.DATE)
    @Schema(description = "修改日期")
    private Date updateTime;

    @TableField(value = "task_child_id")
    @Column(comment="子任务id", type = MySqlTypeConstant.BIGINT , isNull = false )
    private Long taskChildId;

    @TableField(value = "copy_date")
    @Column(comment="备份时间", type = MySqlTypeConstant.DATE, isNull = false )
    private Date copyDate;

    @Column(comment = "业务类型: 0绩效 1科室成本", length = 1,
            type = MySqlTypeConstant.VARCHAR, defaultValue = "0")
    @Schema(description = "业务类型: 0绩效 1科室成本")
    private String type = "0";
}
