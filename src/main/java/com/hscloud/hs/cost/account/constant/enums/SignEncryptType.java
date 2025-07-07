package com.hscloud.hs.cost.account.constant.enums;

import lombok.Getter;

/**
 * 加密字符串枚举类
 * @author banana
 * @create 2023-09-19 16:44
 */
@Getter
public enum SignEncryptType {
    MD5("MD5", "MD5加密字符串"),
    SHA256("SHA256", "SHA256加密字符串"),
    SM3("SM3", "SM3加密字符串");

    private final String code;
    private final String desc;

    SignEncryptType(String code, String desc){
        this.code = code;
        this.desc = desc;
    }

}
