package com.hscloud.hs.cost.account.controller.second;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.constant.CacheConstants;
import com.hscloud.hs.cost.account.model.dto.second.CommonBatchIdsDTO;
import com.hscloud.hs.cost.account.model.dto.second.DmoUserPageDTO;
import com.hscloud.hs.cost.account.model.dto.second.UnitTaskUserAddBatchDTO;
import com.hscloud.hs.cost.account.model.dto.second.UnitTaskUserEditBatchDTO;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.second.UnitTaskUser;
import com.hscloud.hs.cost.account.service.second.IUnitTaskUserService;
import com.hscloud.hs.cost.account.utils.RedisUtil;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
* 发放单元任务人员
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/second/unitTaskUser")
@Tag(name = "unitTaskUser", description = "发放单元任务人员")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class UnitTaskUserController {

    private final IUnitTaskUserService unitTaskUserService;
    private final RedisUtil redisUtil;
    @SysLog("发放单元任务人员info")
    @GetMapping("/info/{id}")
    @Operation(summary = "发放单元任务人员info")
    public R<UnitTaskUser> info(@PathVariable Long id) {
        return R.ok(unitTaskUserService.getById(id));
    }

    @SysLog("发放单元任务人员page")
    @GetMapping("/page")
    @Operation(summary = "发放单元任务人员page")
    public R<IPage<UnitTaskUser>> page(PageRequest<UnitTaskUser> pr) {
        return R.ok(unitTaskUserService.page(pr.getPage(),pr.getWrapper()));
    }

    @SysLog("发放单元任务人员list")
    @GetMapping("/list")
    @Operation(summary = "发放单元任务人员list")
    public R<List<UnitTaskUser>> list(PageRequest<UnitTaskUser> pr) {
        return R.ok(unitTaskUserService.list(pr.getWrapper().lambda().orderByAsc(UnitTaskUser::getSortNum)));
    }

    @SysLog("发放单元任务人员add")
    @PostMapping("/add")
    @Operation(summary = "发放单元任务人员add")
    public R add(@RequestBody UnitTaskUser UnitTaskUser)  {
        return R.ok(unitTaskUserService.save(UnitTaskUser));
    }

    @SysLog("发放单元任务人员addBatch")
    @PostMapping("/addBatch")
    @Operation(summary = "发放单元任务人员addBatch")
    public R addBatch(@RequestBody UnitTaskUserAddBatchDTO unitTaskUserAddBatchDTO)  {
        String key = CacheConstants.SEC_USER_CRUD+unitTaskUserAddBatchDTO.getUnitTaskId();
        if(!redisUtil.setLock(key,1,30L, TimeUnit.SECONDS)){
            throw new BizException("人员处理 请勿重复操作");
        }
        try{
            unitTaskUserService.addBatch(unitTaskUserAddBatchDTO);
        }finally {
            redisUtil.unLock(key);
            System.out.println("unLock======");
        }
        return R.ok();
    }

    @SysLog("发放单元任务人员 editBatch")
    @PostMapping("/editBatch")
    @Operation(summary = "发放单元任务人员 editBatch")
    public R editBatch(@RequestBody UnitTaskUserEditBatchDTO unitTaskUserEditBatchDTO)  {
        List<UnitTaskUser> userList = unitTaskUserEditBatchDTO.getUserList();
        if (userList.isEmpty()){
            return R.ok();
        }
        String key = CacheConstants.SEC_USER_CRUD+userList.get(0).getUnitTaskId();
        if(!redisUtil.setLock(key,1,30L, TimeUnit.SECONDS)){
            throw new BizException("人员处理 请勿重复操作");
        }
        try{
            unitTaskUserService.editBatch(unitTaskUserEditBatchDTO);
        }finally {
            redisUtil.unLock(key);
            System.out.println("unLock======");
        }
        return R.ok();
    }


    @SysLog("发放单元任务人员edit")
    @PostMapping("/edit")
    @Operation(summary = "发放单元任务人员edit")
    public R edit(@RequestBody UnitTaskUser unitTaskUser)  {
        return R.ok(unitTaskUserService.updateById(unitTaskUser));
    }

    @SysLog("发放单元任务人员del")
    @PostMapping("/del/{id}")
    @Operation(summary = "发放单元任务人员del")
    public R del(@PathVariable Long id)  {
        UnitTaskUser unitTaskUser = unitTaskUserService.getById(id);
        if (unitTaskUser == null){
            return R.ok();
        }
        String key = CacheConstants.SEC_USER_CRUD+unitTaskUser.getUnitTaskId();
        if(!redisUtil.setLock(key,1,30L, TimeUnit.SECONDS)){
            throw new BizException("人员处理 请勿重复操作");
        }
        try{
            unitTaskUserService.delById(id);
        }finally {
            redisUtil.unLock(key);
        }


        return R.ok();
    }

    @SysLog("发放单元任务人员delBatch")
    @PostMapping("/delBatch")
    @Operation(summary = "发放单元任务人员delBatch 1,2,3")
    public R delBatch(@RequestBody CommonBatchIdsDTO commonBatchIdsDTO)  {
        String ids = commonBatchIdsDTO.getIds();
        List<Long> idsList = Arrays.stream(ids.split(","))
                .map(Long::valueOf)
                .collect(Collectors.toList());
        unitTaskUserService.delBatchByIds(idsList);
        return R.ok();
    }

    @SysLog("人员挑选page")
    @GetMapping("/userPage")
    @Operation(summary = "人员挑选page")
    public R<IPage<UnitTaskUser>> userPage(DmoUserPageDTO dmoUserPageDTO) {
        return R.ok(unitTaskUserService.userPage(dmoUserPageDTO));
    }



}