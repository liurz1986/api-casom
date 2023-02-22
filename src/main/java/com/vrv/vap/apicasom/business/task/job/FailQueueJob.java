package com.vrv.vap.apicasom.business.task.job;

import com.alibaba.fastjson.JSONObject;
import com.vrv.vap.apicasom.business.task.bean.HwSyncErrorLog;
import com.vrv.vap.apicasom.business.task.bean.MeetingQueueVo;
import com.vrv.vap.apicasom.business.task.service.HwSyncErrorLogService;
import com.vrv.vap.apicasom.business.task.service.MeetingHttpService;
import com.vrv.vap.apicasom.business.task.service.impl.MeetingHttpServiceImpl;
import com.vrv.vap.apicasom.frameworks.util.QueueUtil;
import com.vrv.vap.jpa.common.UUIDUtils;
import com.vrv.vap.jpa.spring.SpringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author: 梁国露
 * @since: 2023/2/20 14:29
 * @description:
 */
@Component
public class FailQueueJob implements CommandLineRunner {

    // 日志
    private Logger logger = LoggerFactory.getLogger(FailQueueJob.class);

    @Autowired
    private HwSyncErrorLogService hwSyncErrorLogService;

    @Override
    public void run(String... args) throws Exception {
        new Thread(new Runnable() {
            @Override
            public void run() {
                failQueue();
            }
        }).start();
    }

    public void failQueue() {
        while (true) {
            MeetingQueueVo meetingQueueVo = QueueUtil.poll();
            if (meetingQueueVo == null) {
                continue;
            }
            String method = meetingQueueVo.getMethod();
            String param = meetingQueueVo.getParam();
            int errorNum = meetingQueueVo.getErrorNum();
            if(errorNum<3){
                try {
                    Class cls = SpringUtil.getBean(MeetingHttpService.class).getClass();
                    if (method.contains("List")) {
                        Method m = cls.getDeclaredMethod(method, String.class, String.class,Integer.class);
                        JSONObject json = JSONObject.parseObject(param);
                        String startTime = json.getString("startTime");
                        String endTime = json.getString("endTime");
                        m.invoke(SpringUtil.getBean(MeetingHttpService.class), startTime, endTime,(errorNum+1));
                    } else if ("getToken".equals(method)) {
                        Method m = cls.getDeclaredMethod(method,Integer.class);
                        m.invoke(SpringUtil.getBean(MeetingHttpService.class),(errorNum+1));
                    } else {
                        Method m = cls.getDeclaredMethod(method, String.class,Integer.class);
                        JSONObject json = JSONObject.parseObject(param);
                        String id = json.getString("id");
                        m.invoke(SpringUtil.getBean(MeetingHttpService.class), id,(errorNum+1));
                    }
                } catch (Exception ex) {
                    logger.error("接口{}，调用失败！失败次数={}", method, (errorNum+1));
                }
            }
            if (errorNum == 3) {
                // 记录错误日志（表）
                HwSyncErrorLog hwSyncErrorLog = new HwSyncErrorLog();
                hwSyncErrorLog.setId(UUIDUtils.get32UUID());
                hwSyncErrorLog.setErrorMethod(meetingQueueVo.getMethod());
                hwSyncErrorLog.setErrorParam(meetingQueueVo.getParam());
                hwSyncErrorLog.setErrorMsg(meetingQueueVo.getErrorMsg());
                hwSyncErrorLog.setErrorTime(new Date());
                hwSyncErrorLogService.save(hwSyncErrorLog);
            }
            // 执行一个休息10分钟
//            try {
//                TimeUnit.MINUTES.sleep(10);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        }
    }
}
