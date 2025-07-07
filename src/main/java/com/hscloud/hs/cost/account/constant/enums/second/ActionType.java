package com.hscloud.hs.cost.account.constant.enums.second;

import lombok.Getter;

/**
 * 批量保存 操作符
 */
@Getter
public enum ActionType {
	add("新增"),
	edit("修改"),
	del("删除");

	private final String name;
	private ActionType(String name){
		this.name = name;
	}

	public static ActionType getByCode(String code) {
		for (ActionType en : ActionType.values()) {
			if (en.toString().equals(code)) {
				return en;
			}
		}
		return null;
	}
}
