package com.vrv.vap.apicasom.business.task.job;

import com.vrv.vap.apicasom.business.task.bean.ZkySend;
import com.vrv.vap.apicasom.business.task.bean.ZkyUnitBean;
import com.vrv.vap.apicasom.business.task.service.*;
import com.vrv.vap.apicasom.business.task.service.impl.MeetingHttpServiceImpl;
import com.vrv.vap.apicasom.frameworks.util.RedisUtils;
import com.vrv.vap.jpa.common.DateUtil;
import com.vrv.vap.jpa.common.UUIDUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    // 日志
    private Logger logger = LoggerFactory.getLogger(ZkySendDataJob.class);

    @Autowired
    private ZkySendDataService zkySendDataService;

    @Autowired
    private ZkySendService zkySendService;


    @Value("${hw.send.url}")
    private String zkySendUrl;

    @Value("${hw.send.local-url}")
    private String zkySendLocalUrl;

    @Scheduled(cron = "${hw.send.time}")
    public void getZkySend(){
        logger.info("中科院文件信息同步开始");
        Date date = new Date();
        String endTime = DateUtil.format(date,DateUtil.DEFAULT_DATE_PATTERN);
        String startTime = DateUtil.format(DateUtil.addDay(date,-1),DateUtil.DEFAULT_DATE_PATTERN);
        List<ZkySend> zkySends = new ArrayList<>();
        //
        List<ZkySend> zkySendList = zkySendDataService.getZkySend(startTime,endTime,"院部机关",zkySendUrl);
        List<ZkySend> zkyList = zkySendDataService.getZkySend(startTime,endTime,"全院",zkySendUrl);

        if(CollectionUtils.isNotEmpty(zkySendList)){
            zkySendList.stream().forEach(item->{
                String id = UUIDUtils.get32UUID();
                item.setId(id);
                item.setSendRegion(1);
            });
            zkySends.addAll(zkySendList);
        }

        if(CollectionUtils.isNotEmpty(zkyList)){
            zkyList.stream().forEach(item->{
                String id = UUIDUtils.get32UUID();
                item.setId(id);
                item.setSendRegion(1);
            });
            zkySends.addAll(zkyList);
        }
        logger.info("中科院文件信息同步，远程文件同步，数据量={}",zkySends.size());
        List<ZkySend> zkySendLocalList = zkySendDataService.getZkySend(startTime,endTime,"院部机关",zkySendLocalUrl);
        List<ZkySend> zkyLocalList = zkySendDataService.getZkySend(startTime,endTime,"全院",zkySendLocalUrl);

        if(CollectionUtils.isNotEmpty(zkySendLocalList)){
            zkySendLocalList.stream().forEach(item->{
                String id = UUIDUtils.get32UUID();
                item.setId(id);
                item.setSendRegion(0);
            });
            zkySends.addAll(zkySendLocalList);
        }

        if(CollectionUtils.isNotEmpty(zkyLocalList)){
            zkyLocalList.stream().forEach(item->{
                String id = UUIDUtils.get32UUID();
                item.setId(id);
                item.setSendRegion(0);
            });
            zkySends.addAll(zkyLocalList);
        }
        logger.info("中科院文件信息同步，本地文件同步，数据量（远程+本地）={}",zkySends.size());

        if(CollectionUtils.isNotEmpty(zkySends)){
            zkySendService.save(zkySends);
        }
        logger.info("中科院文件信息同步，文件信息保存");

    }
}
