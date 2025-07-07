package com.hscloud.hs.cost.account.constant.enums;

/**
 * 二次审核状态
 */
public enum SecondDistributionTaskStatusEnum {

    UNCOMMITTED("UNCOMMITTED","未提交"),
    PENDING_ASSIGNMENT("PENDING_ASSIGNMENT","待分配"),
    PENDING_APPROVAL("PENDING_APPROVAL","待审批"),
    APPROVAL_REJECTED("APPROVAL_REJECTED","审批驳回"),
    APPROVAL_APPROVED("APPROVAL_APPROVED","审批通过"),
    UNDERWAY("UNDERWAY","进行中"),
    FINISHED("FINISHED","已完成");
    private final String code;

    private final String description;

    SecondDistributionTaskStatusEnum(String code,String description) {
        this.code = code;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getCode() {
        return code;
    }
    // 根据code获取description的静态方法
    public static String getDescriptionByCode(String code) {
        for (SecondDistributionTaskStatusEnum status : SecondDistributionTaskStatusEnum.values()) {
            if (status.getCode().equals(code)) {
                return status.getDescription();
            }
        }
        return null; // 或者抛出异常，视情况而定
    }

}
