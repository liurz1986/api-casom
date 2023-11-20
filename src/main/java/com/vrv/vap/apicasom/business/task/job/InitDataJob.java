package com.vrv.vap.apicasom.business.task.job;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vrv.vap.apicasom.business.task.bean.TimeBean;
import com.vrv.vap.apicasom.business.task.service.HwMeetingDataService;
import com.vrv.vap.apicasom.business.task.service.MeetingHttpService;
import com.vrv.vap.apicasom.business.task.service.impl.ReservationHwMeetingDataServiceImpl;
import com.vrv.vap.apicasom.frameworks.util.MeetingUtil;
import com.vrv.vap.apicasom.frameworks.util.RedisUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * 手动补全历史会议数据
 *
 * @author: 梁国露
 * @since: 2023/2/16 17:04
 * @description:
 */
@Component
@Order(value = 2)
public class InitDataJob implements CommandLineRunner {
    // 日志
    private Logger logger = LoggerFactory.getLogger(InitDataJob.class);

    /**
     * gson对象
     */
    private static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();



    @Autowired
    private HwMeetingDataService historyHwMeetingDataService;

    @Autowired
    private MeetingHttpService meetingHttpService;

    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private ReservationHwMeetingDataServiceImpl reservationHwMeetingDataService;

    /**
     * 是否执行手动历史数据补全
     */
    @Value("${hw.meeting.hiscompletion.status:false}")
    private Boolean hisStatus;
    /**
     * 手动历史数据补全开始时间
     */
    @Value("${hw.meeting.hiscompletion.starttime}")
    private String hisStarttime;
    /**
     * 手动历史数据补全结束时间
     */
    @Value("${hw.meeting.hiscompletion.endtime}")
    private String hisEndtime;


    @Override
    public void run(String... args) throws Exception {
        new Thread(new Runnable() {
            @Override
            public void run() {
                initData();
            }
        }).start();
    }

    /**
     * 初始化任务
     */
    public void initData(){
            // 历史会议数据补全
            hisMeetingHandle();

    }
    private void hisMeetingHandle() {
        try{
            if(!hisStatus ){
                logger.info("不执行手动历史数据补全");
                return;
            }
            if(StringUtils.isEmpty(hisStarttime)||StringUtils.isEmpty(hisEndtime)){
                logger.info("手动历史数据补全,开始时间或结束时间存在空的情况，不执行补全");
                return;
            }
            TimeBean time = new TimeBean();
            time.setStartTime(hisStarttime);
            time.setEndTime(hisEndtime);
            // 判断是不是重复补全
            Object obj =  redisUtils.get("hiscompletion");
            if(null != obj){
                String old = String.valueOf(obj);
                String newData = JSON.toJSONString(time);
                if(newData.equals(old)){
                    logger.info("同步时间与上次相同，不执行手动历史会议数据补全");
                    return;
                }
            }
            // 执行补全操作
            hisHandleData(time);
            // redis记录当前状态，避免重复补全
            redisUtils.set("hiscompletion", JSON.toJSONString(time));
        }catch (Exception e){
            logger.error("历史会议手动补全异常",e);
        }
    }


    /**
     * 历史会议补全
     * @param time
     */
    public void hisHandleData(TimeBean time){
        // 处理数据查询时间
        String startTime = time.getStartTime();
        String endTime = time.getEndTime();
        logger.warn("补全历史数据开始!开始时间={}，结束时间={}",startTime,endTime);
        String token = meetingHttpService.getToken(0);
        if(StringUtils.isEmpty(token)){
            logger.error("获取token为空,请确认！");
            return;
        }
        logger.info("token的值："+token);
        MeetingUtil.token= token;
        // 处理数据
        List<String> ids = historyHwMeetingDataService.queryMeetingIds(startTime,endTime);
        logger.warn("补全历史数据 会议ID有{}个！",ids.size());
        if(CollectionUtils.isNotEmpty(ids)){
            historyHwMeetingDataService.handleMeetingInfo(ids);
            logger.warn("补全历史数据 会议详情 完成！");
            historyHwMeetingDataService.handleMeetingAlarm(ids);
            logger.warn("补全历史数据 会议告警 完成！");
        }
    }
}
