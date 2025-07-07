<#assign entityCodeBig = entityCode?cap_first>
package ${comPathDot}.mapper${childSufDot};

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import ${comPathDot}.model.entity${childSufDot}.${entityCodeBig};
import org.apache.ibatis.annotations.Mapper;

/**
* ${entityName} Mapper 接口
*
*/
@Mapper
public interface ${entityCodeBig}Mapper extends BaseMapper<${entityCodeBig}> {

}

