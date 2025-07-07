package com.hscloud.hs.cost.account.constant.enums.kpi;

/**
 * @Classname ErrorCodeEnum
 * @Description TODO
 * @Date 2025/4/16 13:55
 * @Created by sch
 */

public enum ErrorCodeEnum {
    NULL_PARAM(900, "参数不能为空"),
    DUPLICATE_USER(9011, "用户已存在"),
    DOWN_TEMPLATE_01(500,"服务异常，模板下载失败!"),
    SERVICE_EXCEPTION(500,"服务异常"),
    DOWN_POSITION_CHECKLIST_01(400,"上传文件不能为空");
    private final int code;

    private final String description;

    ErrorCodeEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}


