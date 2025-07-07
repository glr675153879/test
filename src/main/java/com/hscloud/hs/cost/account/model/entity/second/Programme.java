package com.hscloud.hs.cost.account.model.entity.second;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.hscloud.hs.cost.account.model.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hf
 * @since 2023-10-04
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "方案")
@TableName("sec_programme")
public class Programme extends BaseEntity<Programme> {

    private static final long serialVersionUID = 1L;

    @Column(comment = "方案名称")
    @Schema(description = "方案名称")
    private String name;

    @Column(comment = "分组id")
    @Schema(description = "分组id")
    private String planGroupId;

    @Column(comment = "发放单元")
    @Schema(description = "发放单元 1,2,3")
    private String grantUnitIds;

    @Column(comment = "发放单元名称")
    @Schema(description = "发放单元名称 a,b,c")
    private String grantUnitNames;

    @Column(comment = "是否分配管理层绩效")
    @Schema(description = "是否分配管理层绩效")
    private String ifContainLeader;

    @Column(comment = "是否公共模板")
    @Schema(description = "是否公共模板")
    private String ifCommon;

    @Column(comment = "是否停用 0启用 1停用")
    @Schema(description = "是否停用 0启用 1停用")
    private String status;

    //发放单元方案的字段
    @Column(comment = "父id")
    @Schema(description = "父id")
    private Long parentId;

    @Column(comment = "发放单元id")
    @Schema(description = "发放单元id")
    private Long grantUnitId;

    @Column(comment = "是否上报方式")
    @Schema(description = "是否上报方式")
    private String ifUpload;

    @Column(comment = "更新人工号")
    @Schema(description = "更新人工号")
    private String updateJobNumber;

    @TableField(exist = false)
    List<ProgProject> progProjectList = new ArrayList<>();
}
