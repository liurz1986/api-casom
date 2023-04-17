package com.vrv.vap.apicasom.business.task.job;

import com.vrv.vap.apicasom.business.task.bean.ZkySend;
import com.vrv.vap.apicasom.business.task.bean.ZkyUnitBean;
import com.vrv.vap.apicasom.business.task.service.*;
import com.vrv.vap.apicasom.frameworks.util.RedisUtils;
import com.vrv.vap.jpa.common.DateUtil;
import com.vrv.vap.jpa.common.UUIDUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author: 梁国露
 * @since: 2023/2/17 15:12
 * @description: 收发件消息
 */
@Component
@EnableScheduling
public class ZkySendDataJob {

    @Autowired
    private ZkySendDataService zkySendDataService;

    @Autowired
    private ZkySendService zkySendService;

    @Scheduled(cron = "${hw.send.time}")
    public void getZkySend(){
        Date date = new Date();
        String endTime = DateUtil.format(date,DateUtil.DEFAULT_DATE_PATTERN);
        String startTime = DateUtil.format(DateUtil.addDay(date,-1),DateUtil.DEFAULT_DATE_PATTERN);
        List<ZkySend> zkySends = new ArrayList<>();
        // 发件
        List<ZkySend> zkySendList = zkySendDataService.getZkySend(startTime,endTime,"发件");
        List<ZkySend> zkyList = zkySendDataService.getZkySend(startTime,endTime,"收文");

        if(CollectionUtils.isNotEmpty(zkySendList)){
            zkySendList.stream().forEach(item->{
                String id = UUIDUtils.get32UUID();
                item.setId(id);
            });
            zkySends.addAll(zkySendList);
        }

        if(CollectionUtils.isNotEmpty(zkyList)){
            zkyList.stream().forEach(item->{
                String id = UUIDUtils.get32UUID();
                item.setId(id);
            });
            zkySends.addAll(zkyList);
        }

        if(CollectionUtils.isNotEmpty(zkySends)){
            zkySendService.save(zkySends);
        }
    }
}
