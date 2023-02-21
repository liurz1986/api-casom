package com.vrv.vap.apicasom.business.task.service;

import com.vrv.vap.apicasom.business.task.bean.SystemConfig;

import java.util.List;

/**
 * @author: 梁国露
 * @since: 2023/2/21 15:53
 * @description:
 */
public interface SystemConfigService {
    public String getStaticConfig(String key, List<SystemConfig> systemConfigs);

    public List<SystemConfig> getStaticConfigs();

    /**
     * 当前项目名称
     * @return
     */
    public String getCurrentConfig(List<SystemConfig> systemConfigs) ;

    /**
     * 通过conf_id 查询配置项的值
     * @param confId
     * @return
     */
    public String getSysConfigById(String confId);
}
