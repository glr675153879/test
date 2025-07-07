package com.hscloud.hs.cost.account.model.dto.dataReport;

import com.dingtalk.api.response.OapiV2UserGetResponse;
import com.pig4cloud.pigx.admin.api.entity.DeptThird;
import com.pig4cloud.pigx.admin.api.entity.SysUser;
import lombok.Data;

import javax.persistence.Entity;
import java.util.List;

@Data
@Entity
public class SysDeptDto {
    private boolean userSelectable;
    private boolean deptSelectable;
    private boolean roleSelectable;
    private List<DeptThird> deptList;
    private List<SysUser> userList;
    private List<OapiV2UserGetResponse.UserRole> roleList;

}
