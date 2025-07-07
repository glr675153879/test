package com.hscloud.hs.cost.account.constant.enums.second;

import lombok.Getter;

/**
 * 用户操作日志记录枚举类
 * @author  lian
 * @date  2024/5/22 17:48
 *
 */

@Getter
public enum UserType {
	Y("Y","编外人员"),
	N("N","编内人员");
	private final String name;
	private final String code;
	UserType(String code, String name){
		this.name = name;
		this.code = code;
	}

	public static UserType getByCode(String code) {
		for (UserType en : UserType.values()) {
			if (en.toString().equals(code)) {
				return en;
			}
		}
		return null;
	}
}
