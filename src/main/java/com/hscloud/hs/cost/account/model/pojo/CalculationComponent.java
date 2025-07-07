package com.hscloud.hs.cost.account.model.pojo;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * @author Admin
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CalculationItem.class, name = "item"),
        @JsonSubTypes.Type(value = CalculationIndex.class, name = "index")
})
public interface CalculationComponent<T> {

    /**
     * 获取类型
     * @return 类型
     */
    String getType();



    /**
     * 获取计算组件
     * @return 计算组件
     */
    T getCalculationComponent();

}
