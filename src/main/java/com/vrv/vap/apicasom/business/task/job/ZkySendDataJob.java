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

import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    private ZkyUnitService zkyUnitService;


    @Value("${hw.send.url}")
    private String zkySendUrl;

    @Value("${hw.send.local-url}")
    private String zkySendLocalUrl;

    private Map<String,String> zkyCityMap= new HashMap<>();

    private int errorCount = 0;

    @Scheduled(cron = "${hw.send.time}")
    public void getZkySend(){
        errorCount = 0;
        Date date = new Date();
        // 时间范围：当前时间向后退一天到当前时间(防止遗漏：当前时间向后退一天后在减去10分钟作为开始时间) 2023-6-6
        String endTime = DateUtil.format(date,DateUtil.DEFAULT_DATE_PATTERN);
        String startTime = DateUtil.format(new Date(DateUtil.addDay(date,-1).getTime()-10*60*1000),DateUtil.DEFAULT_DATE_PATTERN);
        try{
            excZkySend(endTime,startTime);
        }catch (Exception e){
            logger.error("中科院文件信息同步失败：{}",e);
            exceptionHandle(endTime,startTime);
        }

    }

    /**
     * 异常后重复执行，执行三次，失败就不执行了,日志输出
     * @param endTime
     * @param startTime
     */
    private void exceptionHandle(String endTime, String startTime) {
        logger.info("中科院文件信息同步失败后处理开始");
        errorCount++;
        try{
            excZkySend(endTime,startTime);
            errorCount = 0;
            logger.info("中科院文件信息同步失败后处理成功");
        }catch(Exception e){
            if(errorCount > 3){
                logger.error("中科院文件信息同步失败,重复执行三次还是异常，不处理了：{}",e);
                errorCount = 0;
                return;
            }
            exceptionHandle(endTime,startTime);
        }
    }

    private void excZkySend( String endTime,String startTime){
       getZkyCityMap();
       logger.info("中科院文件信息同步开始，时间范围："+startTime+"-"+endTime);
       List<ZkySend> zkySends = new ArrayList<>();
       List<ZkySend> zkySendList = zkySendDataService.getZkySend(startTime,endTime,"院部机关",zkySendUrl);
       List<ZkySend> zkyList = zkySendDataService.getZkySend(startTime,endTime,"全院",zkySendUrl);
       if(CollectionUtils.isNotEmpty(zkySendList)){
           zkySendList.stream().forEach(item->{
               String id = UUIDUtils.get32UUID();
               item.setId(id);
               item.setSendRegion(1);
               String branch = zkyCityMap.get(item.getOrgName());
               item.setBranch(branch);
           });
           zkySends.addAll(zkySendList);
       }

       if(CollectionUtils.isNotEmpty(zkyList)){
           zkyList.stream().forEach(item->{
               String id = UUIDUtils.get32UUID();
               item.setId(id);
               item.setSendRegion(1);
               String branch = zkyCityMap.get(item.getOrgName());
               item.setBranch(branch);
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
               String branch = zkyCityMap.get(item.getOrgName());
               item.setBranch(branch);
           });
           zkySends.addAll(zkySendLocalList);
       }

       if(CollectionUtils.isNotEmpty(zkyLocalList)){
           zkyLocalList.stream().forEach(item->{
               String id = UUIDUtils.get32UUID();
               item.setId(id);
               item.setSendRegion(0);
               String branch = zkyCityMap.get(item.getOrgName());
               item.setBranch(branch);
           });
           zkySends.addAll(zkyLocalList);
       }
       logger.info("中科院文件信息同步，本地文件同步，数据量（远程+本地）={}",zkySends.size());

       if(CollectionUtils.isNotEmpty(zkySends)){
           zkySendService.save(zkySends);
       }
       logger.info("中科院文件信息同步，文件信息保存");

   }
    /**
     * 获取城市分院对照
     * @return
     */
    public void getZkyCityMap(){
        List<ZkyUnitBean> list = zkyUnitService.findAll();
        Map<String,List<ZkyUnitBean>> zkyUnitMap = list.stream().collect(Collectors.groupingBy(ZkyUnitBean::getName));
        for(Map.Entry<String,List<ZkyUnitBean>> entry : zkyUnitMap.entrySet()){
            if(entry.getValue().size() != 0){
                zkyCityMap.put(entry.getKey(),entry.getValue().get(0).getBranch());
            }
        }
    }
}
