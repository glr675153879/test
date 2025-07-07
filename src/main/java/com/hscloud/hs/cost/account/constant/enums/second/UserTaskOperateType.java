package com.hscloud.hs.cost.account.constant.enums.second;

import lombok.Getter;

/**
 * 用户操作日志记录枚举类
 */
@Getter
public enum UserTaskOperateType {
	ADD("ADD","新增"),
	DELETE("DELETE","删除");
	private final String name;
	private final String code;
	private UserTaskOperateType(String code,String name){
		this.name = name;
		this.code = code;
	}

	public static UserTaskOperateType getByCode(String code) {
		for (UserTaskOperateType en : UserTaskOperateType.values()) {
			if (en.toString().equals(code)) {
				return en;
			}
		}
		return null;
	}
}
