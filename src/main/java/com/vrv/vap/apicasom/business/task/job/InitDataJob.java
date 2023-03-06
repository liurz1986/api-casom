package com.vrv.vap.apicasom.business.task.job;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vrv.vap.apicasom.business.task.bean.ProcessJob;
import com.vrv.vap.apicasom.business.task.bean.TimeBean;
import com.vrv.vap.apicasom.business.task.service.HwMeetingDataService;
import com.vrv.vap.apicasom.business.task.service.SystemConfigService;
import com.vrv.vap.jpa.common.DateUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author: 梁国露
 * @since: 2023/2/16 17:04
 * @description:
 */
@Component
public class InitDataJob implements CommandLineRunner {
    // 日志
    private Logger logger = LoggerFactory.getLogger(InitDataJob.class);

    /**
     * gson对象
     */
    private static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private HwMeetingDataService historyHwMeetingDataService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
        // 死循环
        while (true){
            String sql = "select job_name,job_cron from process_job where status = 1 and job_name = 'InitHwMeetingJob';";
            List<ProcessJob> list = jdbcTemplate.query(sql,new BeanPropertyRowMapper<ProcessJob>(ProcessJob.class));
            if(CollectionUtils.isEmpty(list)){
                try {
                    TimeUnit.MINUTES.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            String configValue = systemConfigService.getSysConfigById("meeting_time_conf");
            // 存在配置，且配置开启
            TimeBean time = null;
            if(StringUtils.isBlank(configValue)){
                time = new TimeBean();
                logger.error("时间格式配置错误，tb_conf配置项，meeting_time_conf 未配置");
            }else{
                time = gson.fromJson(configValue,TimeBean.class);
            }
            handleData(time);
            updateJobStatus();
        }
    }

    public void updateJobStatus(){
        String sql = "update process_job set status = 0 where job_name = 'InitHwMeetingJob';";
        jdbcTemplate.execute(sql);
    }

    /**
     * 查询数据
     * @param time
     */
    public void handleData(TimeBean time){
        // 处理数据查询时间
        String startTime = time != null ?time.getStartTime():null;
        String endTime = time != null ?time.getEndTime():null;

        if(StringUtils.isBlank(startTime) && StringUtils.isBlank(endTime)){
            // 开始时间为空，往前推1年
            startTime = DateUtil.format(DateUtil.addMouth(new Date(),-12),DateUtil.DEFAULT_DATE_PATTERN);
            // 结束时间为空，定义为当前时间
            endTime = DateUtil.format(new Date(),DateUtil.DEFAULT_DATE_PATTERN);
        }else if(StringUtils.isNotBlank(startTime) && StringUtils.isBlank(endTime)){
            // 结束时间为空，定义为当前时间
            endTime = DateUtil.format(new Date(),DateUtil.DEFAULT_DATE_PATTERN);
        }else if(StringUtils.isBlank(startTime) && StringUtils.isNotBlank(endTime)){
            // 开始时间为空，往前推1年
            try {
                startTime = DateUtil.format(DateUtil.addMouth(DateUtil.parseDate(endTime,DateUtil.DEFAULT_DATE_PATTERN),-12),DateUtil.DEFAULT_DATE_PATTERN);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        logger.warn("补全历史数据开始!开始时间={}，结束时间={}",startTime,endTime);

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
