package com.vrv.vap.apicasom.business.task.service.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.vrv.vap.apicasom.business.task.bean.ZkySend;
import com.vrv.vap.apicasom.business.task.bean.ZkyUnitBean;
import com.vrv.vap.apicasom.business.task.service.ZkySendDataService;
import com.vrv.vap.apicasom.business.task.service.ZkySendService;
import com.vrv.vap.apicasom.business.task.service.ZkyUnitService;
import com.vrv.vap.apicasom.frameworks.util.HttpClientUtils;
import com.vrv.vap.jpa.common.DateUtil;
import com.vrv.vap.jpa.common.UUIDUtils;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: 梁国露
 * @since: 2023/3/8 18:09
 * @description:
 */
@Service
public class ZkySendDataServiceImpl implements ZkySendDataService {

    // 日志
    private Logger logger = LoggerFactory.getLogger(MeetingHttpServiceImpl.class);

    /**
     * gson对象
     */
    private static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();


    @Autowired
    private ZkySendService zkySendService;

    @Autowired
    private ZkyUnitService zkyUnitService;

    @Value("${hw.send.url}")
    private String zkySendUrl;

    @Value("${hw.send.local-url}")
    private String zkySendLocalUrl;


    private Map<String,String> zkyCityMap= new HashMap<>();



    /**
     * 定时任务执行收发件数据
     * 2023-06-20
     */
    @Override
    public void excZkySend() throws InterruptedException {
        Date date = new Date();
        // 时间范围：当前时间向后退一天到当前时间
        String endTime = DateUtil.format(date,DateUtil.DEFAULT_DATE_PATTERN);
        String startTime = DateUtil.format(new Date(DateUtil.addDay(date,-1).getTime()),DateUtil.DEFAULT_DATE_PATTERN);
        try{
            getZkyCityMap();
            dataSyncHandle(endTime,startTime);
        }catch (Exception e){
            logger.error("中科院文件信息同步失败",e);
            exceptionHandle(endTime,startTime);
        }
    }

    private List<ZkySend> getZkySend(String startTime,String endTime,String sendScope,String url) {
        List<ZkySend> zkySends = new ArrayList<>();
        Map<String,Object> param = new HashMap<>();
        param.put("startDate",startTime);
        param.put("endDate",endTime);
        param.put("sendScope",sendScope);
        Map<String,String> header = new HashMap<>();
        header.put("Content-type","application/json;charset=UTF-8");
        String res = HttpClientUtils.doPost(url,param,header);

        if(StringUtils.isNotBlank(res)){
            Type typeToken = new TypeToken<List<ZkySend>>(){}.getType();
            List<ZkySend> zkySendList =gson.fromJson(res,typeToken);
            zkySends.addAll(zkySendList);
        }
        return zkySends;
    }

    private void dataSyncHandle(String endTime, String startTime){
        logger.info("中科院文件信息同步开始，时间范围："+startTime+"-"+endTime);
        List<ZkySend> zkySends = new ArrayList<>();
        List<ZkySend> zkySendList = getZkySend(startTime,endTime,"院部机关",zkySendUrl);
        List<ZkySend> zkyList = getZkySend(startTime,endTime,"全院",zkySendUrl);
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
        List<ZkySend> zkySendLocalList = getZkySend(startTime,endTime,"院部机关",zkySendLocalUrl);
        List<ZkySend> zkyLocalList = getZkySend(startTime,endTime,"全院",zkySendLocalUrl);

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

    /**
     * 异常后重复执行，执行三次，失败就不执行了,日志输出
     * @param endTime
     * @param startTime
     */
    private void exceptionHandle(String endTime, String startTime) throws InterruptedException {
        logger.info("中科院文件信息同步失败后处理开始,时间范围："+startTime+"-"+endTime);
        Thread.sleep(10000);
        new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                boolean status = false;
                for(int i =0 ;i < 3; i++){
                    try{
                        dataSyncHandle(endTime,startTime);
                        status = true;
                    }catch(Exception e){
                        Thread.sleep(10000);
                        dataSyncHandle(endTime,startTime);
                    }
                }
                if(status){
                    logger.info("中科院文件信息同步失败后处理成功");
                }else{
                    logger.info("中科院文件信息同步失败后处理失败,时间范围："+startTime+"-"+endTime);
                }
            }
        }).start();
    }
}
