package com.vrv.vap.apicasom.business.task.service.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.vrv.vap.apicasom.business.task.bean.ZkySend;
import com.vrv.vap.apicasom.business.task.bean.ZkyUnitBean;
import com.vrv.vap.apicasom.business.task.dao.ZkySendDao;
import com.vrv.vap.apicasom.business.task.service.ZkySendDataService;
import com.vrv.vap.apicasom.business.task.service.ZkySendService;
import com.vrv.vap.apicasom.business.task.service.ZkyUnitService;
import com.vrv.vap.apicasom.frameworks.util.HttpClientUtils;
import com.vrv.vap.apicasom.frameworks.util.RedisUtils;
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
    private Logger logger = LoggerFactory.getLogger(ZkySendDataServiceImpl.class);

    /**
     * gson对象
     */
    private static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();


    @Autowired
    private ZkySendService zkySendService;

    @Autowired
    private ZkyUnitService zkyUnitService;

    @Autowired
    private ZkySendDao zkySendDao;

    @Value("${hw.send.url}")
    private String zkySendUrl;

    @Value("${hw.send.local-url}")
    private String zkySendLocalUrl;

    @Autowired
    private RedisUtils redisUtils;

    private Map<String,String> zkyCityMap= new HashMap<>();



    /**
     * 定时任务执行收发件数据
     * 2023-06-20
     */
    @Override
    public void excZkySend() throws InterruptedException {
        // 时间范围：前一天的数据 00:00:00--23:59:59
        Date date = new Date();
        String nextDate = DateUtil.format(new Date(DateUtil.addDay(date,-1).getTime()),DateUtil.Year_Mouth_Day);
        String startTime = nextDate+" 00:00:00";
        String endTime = nextDate+" 23:59:59";
        Map<String,String> time= new HashMap<>();
        time.put("startTime",startTime);
        time.put("endTime",endTime);
        try{
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

    /**
     * 中科院文件信息同步
     * @param endTime
     * @param startTime
     */
    @Override
    public void dataSyncHandle(String endTime, String startTime){
        logger.warn("中科院文件信息同步开始，时间范围："+startTime+"-"+endTime);
        getZkyCityMap();
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
            // 数据去重处理，根据时间
            String dateTime = startTime.split(" ")[0];
            boolean result = isExist(dateTime);
            if(result){ // 存在当前时间数据，删除历史数据
                logger.info("存在当前时间数据，删除历史数据,"+dateTime);
                zkySendDao.deleteRepeatTimeData(dateTime);
            }
            zkySendService.save(zkySends);
        }
        logger.warn("中科院文件信息同步，文件信息保存");
    }

    private boolean isExist(String dateTime) {
       return zkySendDao.isRepeat(dateTime);
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
                        logger.error("同步失败：{}",e);
                    }
                    Thread.sleep(10000);
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
