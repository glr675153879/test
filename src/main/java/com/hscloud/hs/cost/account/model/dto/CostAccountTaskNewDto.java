package com.hscloud.hs.cost.account.model.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author 小小w
 * @date 2023/11/23 17:30
 */
@Data
@Schema(description = "核算任务对象Dto(新)")
public class CostAccountTaskNewDto {
    private static final long serialVersionUID = 1L;

    @Schema(description = "任务id")
    private Long taskId;

    @Schema(description = "核算任务名称")
    private String accountTaskName;

    @Schema(description = "核算开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime accountStartTime;

    @Schema(description = "核算结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime accountEndTime;

    @Schema(description = "核算任务分组")
    private List<TaskGroupInfo> taskGroupInfoList;

    @Schema(description = "编辑到达层数")
    private String step;

    @Data
    @Schema(description = "关联分组参数")
    public static class TaskGroupInfo {

        @Schema(description = "主键id")
        private Long taskGroupInfoId;

        @Schema(description = "任务分组id")
        private Long taskGroupId;

        @Schema(description = "核算方案id")
        private Long planId;

        @Schema(description = "核算对象类型")
        private String accountObjectType;

        @Schema(description = "核算对象id集合")
        private List<objectInfo> accountObjectIds;

        @Schema(description = "指标信息")
        private List<IndexInfo> indexInfoList;
    }

    @Data
    @Schema(description = "指标参数")
    public static class IndexInfo {

        @Schema(description = "指标id")
        private Long indexId;

        @Schema(description = "是否关联 0:否  1:是")
        private String isRelevance;

        @Schema(description = "关联任务id")
        private Long relevanceTaskId;
    }

    @Data
    @Schema(description = "核算对象信息")
    public static class objectInfo {

        @Schema(description = "id")
        private Long id;

        @Schema(description = "名称")
        private String name;

    }
}
