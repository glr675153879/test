package com.hscloud.hs.cost.account.model.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * Author:  Administrator
 * Date:  2025/3/20 19:27
 */
@Data
public class CostImportErroDto {
    private Long seq;
    private String line;
    private List<String> errorInfo = new ArrayList<>();
}
