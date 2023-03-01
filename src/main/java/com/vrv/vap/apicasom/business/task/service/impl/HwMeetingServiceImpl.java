package com.vrv.vap.apicasom.business.task.service.impl;

import com.vrv.vap.apicasom.business.task.bean.ZkyUnitBean;
import com.vrv.vap.apicasom.business.task.service.HwMeetingService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: 梁国露
 * @since: 2023/3/1 10:31
 * @description:
 */
@Service
public class HwMeetingServiceImpl implements HwMeetingService {

    public static String token = null;

    public static Map<String, ZkyUnitBean> zkyUnitBeanMap = new HashMap<>();

    @Override
    public void updateToken(String newToken) {
        token = newToken;
    }


    @Override
    public void updateCity(Map<String, ZkyUnitBean> map) {
        zkyUnitBeanMap.clear();
        for (Map.Entry<String, ZkyUnitBean> entry : map.entrySet()) {
            zkyUnitBeanMap.put(entry.getKey(), entry.getValue());
        }
    }
}
