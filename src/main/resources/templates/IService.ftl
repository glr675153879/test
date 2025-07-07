<#assign entityCodeBig = entityCode?cap_first>
package ${comPathDot}.service${childSufDot};

import com.baomidou.mybatisplus.extension.service.IService;
import ${comPathDot}.model.entity${childSufDot}.${entityCodeBig};

/**
* ${entityName} 服务接口类
*/
public interface I${entityCodeBig}Service extends IService<${entityCodeBig}> {

}
