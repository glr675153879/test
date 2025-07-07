package com.hscloud.hs.cost.account.model.vo.userAttendance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 自定义字段
 *
 * @JC
 * @since 2024-05-29
 */
@Data
@Schema(description = "自定义字段")
public class CustomFieldVO {
    private String id;
    private String name;
    private String num;

    public CustomFieldVO(String id, String name, String num) {
        this.id = id;
        this.name = name;
        this.num = num;
    }

    public CustomFieldVO() {

    }
}
