package com.hscloud.hs.cost.account.model.dto.userAttendance;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LockDataDto {

    @JsonProperty("q[dt]")
    private String dt;
}
