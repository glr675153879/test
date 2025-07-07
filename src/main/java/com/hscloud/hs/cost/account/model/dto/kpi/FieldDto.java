package com.hscloud.hs.cost.account.model.dto.kpi;

import lombok.Data;

import java.lang.reflect.Field;

/**
 * @Classname FieldDto
 * @Description TODO
 * @Date 2023-09-29 17:34
 * @Created by sch
 */
@Data
public class FieldDto {
    private Field field;
    private Integer seq;
}
