package com.vrv.vap.apicasom.business.task.service;

import com.vrv.vap.apicasom.business.task.bean.HwMeetingInfo;
import com.vrv.vap.apicasom.business.task.bean.ZkyUnitBean;
import com.vrv.vap.jpa.baseservice.BaseService;

import java.util.Map;

/**
 * @author: 梁国露
 * @since: 2023/2/17 09:46
 * @description:
 */
public interface ZkyUnitService extends BaseService<ZkyUnitBean, String> {
    /**
     * 初始化城市信息
     */
    public Map<String,ZkyUnitBean> initCity();
}
