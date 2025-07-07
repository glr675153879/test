package com.hscloud.hs.cost.account.model.entity.userAttendance;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.hscloud.hs.cost.account.model.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 人员考勤配置表
 *
 * @JC
 * @since 2024-05-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cost_user_attendance_config")
@Schema(description = "人员考勤配置表")
public class CostUserAttendanceConfig extends BaseEntity<CostUserAttendanceConfig> {

    private static final long serialVersionUID = 1L;

    @Column(comment = "时间-年月 202406")
    @Schema(description = "时间-年月 202406")
    private String dt;

    @Column(comment = "模式 1：导入 2：采集", defaultValue = "1")
    @Schema(description = "模式 1：导入 2：采集")
    private String pattern;

}
