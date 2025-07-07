package com.hscloud.hs.cost.account.constant.enums.dataReport;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author banana
 * @create 2024-09-12 16:48
 */
@Getter
@AllArgsConstructor
public enum ReportRecordStatusEnum {


    INIT("0", "初始化"),
    UNREPORT("1", "未上报"),
    REPORTED("2", "已上报"),
    EXPIRED("3", "过期"),
    REJECT("4", "驳回"),
    APPROVE("5", "通过");


    private String val;

    private String desc;

}
