package com.hscloud.hs.cost.account.model.entity.second;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.hscloud.hs.cost.account.model.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author hf
 * @since 2023-10-04
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "发放单元操作日志")
@TableName("sec_grant_unit_log")
public class GrantUnitLog extends BaseEntity<GrantUnitLog> {

    private static final long serialVersionUID = 1L;

    @Column(comment = "发放单元Id")
    @Schema(description = "发放单元Id")
    private Long grantUnitId;

    @Column(comment = "操作内容", length=1000)
    @Schema(description = "操作内容")
    private String content;



}
