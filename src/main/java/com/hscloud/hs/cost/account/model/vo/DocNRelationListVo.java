package com.hscloud.hs.cost.account.model.vo;

import com.hscloud.hs.cost.account.model.dto.CommonDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author banana
 * @create 2023-09-11 20:11
 */
@Data
@Schema(description = "医护对应组列表出参")
public class DocNRelationListVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "医生科室单元信息")
    private CommonDTO docInfo;

    @Schema(description = "护理科室单元信息")
    private List<CommonDTO> nurseInfo;

}
