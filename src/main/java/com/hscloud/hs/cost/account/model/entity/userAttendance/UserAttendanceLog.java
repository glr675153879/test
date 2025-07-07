package com.hscloud.hs.cost.account.model.entity.userAttendance;


import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import com.hscloud.hs.cost.account.model.entity.base.BaseEntity;
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
@Schema(description = "人员考勤表变更日志")
@Entity
@Table(name = "user_attendance_log")
public class UserAttendanceLog extends BaseEntity<UserAttendanceLog> {

    private static final long serialVersionUID = -7980490238230844984L;

    @Column(comment = "核算周期")
    @Schema(description = "核算周期")
    private String dt;

    @Column(comment = "操作类型 1：变更")
    @Schema(description = "操作类型 1：变更")
    private String opsType;

    @Column(comment = "操作项")
    @Schema(description = "操作项")
    private String opsItem;

    @Column(comment = "操作人")
    @Schema(description = "操作人")
    private String opsBy;

    @Column(comment = "操作人id")
    @Schema(description = "操作人id")
    private Long opsById;

    @Column(comment = "操作时间")
    @Schema(description = "操作时间")
    private LocalDateTime opsTime;

    @Column(comment = "工号")
    @Schema(description = "工号")
    private String jobNumber;

    @Column(comment = "描述", type = MySqlTypeConstant.TEXT)
    @Schema(description = "描述")
    private String description;

}
