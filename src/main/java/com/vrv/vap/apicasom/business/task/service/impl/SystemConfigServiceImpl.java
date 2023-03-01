package com.vrv.vap.apicasom.business.task.service.impl;

import com.vrv.vap.apicasom.business.task.bean.SystemConfig;
import com.vrv.vap.apicasom.business.task.service.SystemConfigService;
import com.vrv.vap.apicasom.frameworks.feign.AdminFeign;
import com.vrv.vap.common.vo.VData;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service(value="systemConfigService")
public class SystemConfigServiceImpl implements SystemConfigService {
	
	private static Logger logger = LoggerFactory.getLogger(SystemConfigServiceImpl.class);
	
	@Autowired
	AdminFeign adminFeign;
	
	@Override
	public String getStaticConfig(String key ,List<SystemConfig> systemConfigs) {
		if(systemConfigs==null) {
			systemConfigs=this.getStaticConfigs();
		}
		
		SystemConfig systemConfig = null;
		
		if(systemConfigs!=null) {
			for(SystemConfig config : systemConfigs) {
				if(config.getConfId().equals(key))
				{
					systemConfig=config;
					break;
				}
			}
		}
		return systemConfig==null?null:systemConfig.getConfValue();
	}
	
	@Override
	public List<SystemConfig> getStaticConfigs() {
		logger.info("feignAll接口获取前");
		VData<List<SystemConfig>> all = adminFeign.allConfig();
 
		 if("0".equals(all.getCode())) {
			 return all.getData();
		 }
		 return  null;
	}
	
	
	/**
	 * 获取系统对应项目配置
	 * @return
	 */
	@Override
	public String getCurrentConfig(List<SystemConfig> systemConfigs) {
		String staticConfig = this.getStaticConfig("current",systemConfigs);
		if(StringUtils.isEmpty(staticConfig)) {
			staticConfig = this.getStaticConfig("system_version",systemConfigs);
		}
		
		if(StringUtils.isEmpty(staticConfig)) {
			return "vap-soc";
		}else {
			return staticConfig;
		}

	}

	@Override
	public String getSysConfigById(String confId) {
		try {
			VData<SystemConfig> systemConfigVdata = adminFeign.getConfigById(confId);
			SystemConfig systemConfig = systemConfigVdata.getData();
			if(systemConfig != null){
				if(systemConfig.getConfEnable()==1){
					return systemConfig.getConfValue();
				}
			}
		}catch (Exception exception){
			logger.error("getSysConfigById api-admin /system/config/{confId} is not connect");
		}
		return null;
	}

}
