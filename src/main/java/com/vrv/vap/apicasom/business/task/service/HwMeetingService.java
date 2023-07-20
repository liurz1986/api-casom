package com.vrv.vap.apicasom.business.task.service;

import com.vrv.vap.apicasom.business.task.bean.ZkyUnitBean;

import java.util.Map;

/**
 * @author: 梁国露
 * @since: 2023/3/1 10:31
 * @description:
 */
public interface HwMeetingService {

    /**
     * 更新城市信息
     * @param map
     */
    public void updateCity(Map<String, ZkyUnitBean> map);
}
