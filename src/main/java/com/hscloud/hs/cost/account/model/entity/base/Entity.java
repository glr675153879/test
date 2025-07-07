package com.hscloud.hs.cost.account.model.entity.base;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.annotation.ColumnComment;
import com.gitee.sunchenbin.mybatis.actable.annotation.IgnoreUpdate;
import com.gitee.sunchenbin.mybatis.actable.annotation.IsAutoIncrement;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@MappedSuperclass
public abstract class Entity<T extends Model<T>> extends Model<T> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @IsAutoIncrement
    @ColumnComment(value = "ID")
    private Long id;


    @Column(comment = "新增人", length = 20)
    @Schema(description = "新增人")
    @TableField(fill = FieldFill.INSERT)
    @IgnoreUpdate
    private String createBy;

    @Column(comment = "新增日期")
    @Schema(description = "新增日期")
    @TableField(fill = FieldFill.INSERT)
    @IgnoreUpdate
    private LocalDateTime createTime;

    @Column(comment = "修改人", length = 20)
    @Schema(description = "修改人")
    @TableField(fill = FieldFill.UPDATE)
    private String updateBy;

    @Column(comment = "修改日期")
    @Schema(description = "修改日期")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    @Column(comment = "删除标记,1:已删除,0:正常", length = 1)
    @Schema(description = "删除标记,1:已删除,0:正常")
    private String delFlag;


    /**
     * 临时object字段
     */
    @TableField(exist = false)
    private Object segment;

    /**
     * 清除通用字段
     */
    public void clearCommonField() {
        this.id = null;
        this.createBy = null;
        this.createTime = null;
        this.updateBy = null;
        this.updateTime = null;
        this.delFlag = null;
    }

}
