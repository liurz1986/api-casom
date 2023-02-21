package com.vrv.vap.apicasom.frameworks.feign;

import com.vrv.vap.apicasom.business.task.bean.SystemConfig;
import com.vrv.vap.common.vo.VData;
import com.vrv.vap.common.vo.VList;
import com.vrv.vap.jpa.web.ResultObjVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;


/**
 * 权限接口调用
 * @author wd-pc
 *
 */
@FeignClient(name = "api-admin",configuration = ConfigurationFegin.class)
public interface AdminFeign {
	/**
	 * 读取系统配置表
	 */
	@RequestMapping(value="/system/config",method = RequestMethod.GET,consumes=MediaType.APPLICATION_JSON_VALUE)
	public VData<List<SystemConfig>> allConfig();

	/**
	 * 读取系统配置表
	 */
	@RequestMapping(value="/system/config/{confId}",method = RequestMethod.GET,consumes=MediaType.APPLICATION_JSON_VALUE)
	public VData<SystemConfig> getConfigById(@PathVariable(value="confId") String confId);

}
