package com.vrv.vap.apicasom.business.task.service.impl;

import com.vrv.vap.apicasom.business.task.bean.ZkyUnitBean;
import com.vrv.vap.apicasom.business.task.service.ZkyUnitService;
import com.vrv.vap.apicasom.business.task.service.repository.ZkyUnitRespository;
import com.vrv.vap.jpa.basedao.BaseRepository;
import com.vrv.vap.jpa.baseservice.impl.BaseServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author: 梁国露
 * @since: 2023/2/17 09:46
 * @description:
 */
@Service
public class ZkyUnitServiceImpl extends BaseServiceImpl<ZkyUnitBean, String> implements ZkyUnitService {
    @Autowired
    private ZkyUnitRespository zkyUnitRespository;

    @Override
    public BaseRepository<ZkyUnitBean, String> getRepository() {
        return zkyUnitRespository;
    }

    @Override
    public Map<String, ZkyUnitBean> initCity() {
        Map<String, ZkyUnitBean> zkyUnitBeanMap = new ConcurrentHashMap<>();
        List<ZkyUnitBean> zkyUnitBeans = findAll();
        Map<String,List<ZkyUnitBean>> zkyUnitMap = zkyUnitBeans.stream().collect(Collectors.groupingBy(ZkyUnitBean::getParticipantName));
        for(Map.Entry<String,List<ZkyUnitBean>> entry : zkyUnitMap.entrySet()){
            zkyUnitBeanMap.put(entry.getKey(),entry.getValue().get(0));
        }
        return zkyUnitBeanMap;
    }
}
