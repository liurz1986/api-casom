package com.vrv.vap.apicasom.business.meeting.service.impl;

import com.vrv.vap.apicasom.business.meeting.service.MeetingService;
import com.vrv.vap.apicasom.business.task.bean.ZkyUnitBean;
import com.vrv.vap.apicasom.business.task.service.ZkyUnitService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: 梁国露
 * @since: 2023/2/16 17:10
 * @description:
 */
@Service
public class MeetingServiceImpl implements MeetingService {

    @Autowired
    private ZkyUnitService zkyUnitService;

    /**
     * 点接入列表中节点名称列表
     *
     * @return
     */
    @Override
    public List<String> getNodeNames() {
        List<ZkyUnitBean> datas = zkyUnitService.findAll();
        if(CollectionUtils.isEmpty(datas)){
            return new ArrayList<>();
        }
        List<String> nodes = new ArrayList<>();
        datas.stream().forEach(item ->{
                    if(!nodes.contains(item.getParticipantName())){
                           nodes.add(item.getParticipantName());
                    }
        });
        return nodes;
    }

    /**
     * 所属分院/地区数据
     *
     * @return
     */
    @Override
    public List<String> getRegions() {
        List<ZkyUnitBean> datas = zkyUnitService.findAll();
        if(CollectionUtils.isEmpty(datas)){
            return new ArrayList<>();
        }
        List<String> regions = new ArrayList<>();
        datas.stream().forEach(item ->{
            if(!regions.contains(item.getCity())){
                regions.add(item.getCity());
            }
        });
        return regions;
    }
}
