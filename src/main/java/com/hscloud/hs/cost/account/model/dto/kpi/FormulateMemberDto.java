package com.hscloud.hs.cost.account.model.dto.kpi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class FormulateMemberDto {
    @JsonProperty("fieldCode")
    private String fieldCode;
    @JsonProperty("fieldType")
    private String fieldType;
    private List<Long> ids;
}
