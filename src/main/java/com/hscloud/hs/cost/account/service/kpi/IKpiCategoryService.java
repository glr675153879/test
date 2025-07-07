package com.hscloud.hs.cost.account.service.kpi;

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.kpi.*;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiCategory;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
* 分组表 服务接口类
*/
public interface IKpiCategoryService extends IService<KpiCategory> {


     Long saveOrUpdateGroup(KpiCategoryDto input);



     List<Tree<Long>> getTreeForCategory(KpiGroupListSearchDto name);


     void deleteGroup(KpiGroupDelDto dto);

     /**
      * 根据分类查询code、name的map数据
      * @param categoryType 分组编码
      * @param categoryCode 分类code
      * @param busiType 业务类型
      * @return map
      */
     Map<String,String> getCodeAndNameMap(String categoryType, String categoryCode, String busiType);


    void copy(CategoryCopyDto dto);
}
