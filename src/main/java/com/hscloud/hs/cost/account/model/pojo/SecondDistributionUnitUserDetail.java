package com.hscloud.hs.cost.account.model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class SecondDistributionUnitUserDetail {

    @Schema(description = "列名")
    private String title;


    @Schema(description = "列数据")
    private List<String> data;
}
