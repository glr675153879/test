package com.hscloud.hs.cost.account.model.entity.base;

import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import javax.persistence.MappedSuperclass;


@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@MappedSuperclass
public abstract class BaseEntity<T extends Entity<T>> extends Entity<T> {

    @Column(comment = "租户ID")
    @Schema(description = "租户ID")
    private Long tenantId;


}
