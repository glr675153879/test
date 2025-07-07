package com.hscloud.hs.cost.account.model.dto.kpi;

import lombok.Data;

import java.util.List;

@Data
public class FieldRelationDto<T> {
    private boolean relation;
    private T object;

    public FieldRelationDto(boolean relation, T object) {
        this.relation = relation;
        this.object = object;
    }
    public FieldRelationDto(T list) {
        this.relation = true;
        this.object = list;
    }
}
