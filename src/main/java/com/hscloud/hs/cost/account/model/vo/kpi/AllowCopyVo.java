package com.hscloud.hs.cost.account.model.vo.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class AllowCopyVo {
    @Schema(description = "是否允许复制")
    private Boolean allow;

    private List<NotAllowCategory> notAllowCategoryList;

    @Data
    public static class NotAllowCategory{
        private String category;
    }
}
