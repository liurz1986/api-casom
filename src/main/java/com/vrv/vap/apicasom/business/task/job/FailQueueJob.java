package com.vrv.vap.apicasom.business.task.job;

import com.alibaba.fastjson.JSONObject;
import com.vrv.vap.apicasom.business.task.bean.MeetingQueueVo;
import com.vrv.vap.apicasom.frameworks.util.QueueUtil;
import com.vrv.vap.jpa.spring.SpringUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * @author: 梁国露
 * @since: 2023/2/20 14:29
 * @description:
 */
@Component
public class FailQueueJob implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        new Thread(new Runnable() {
            @Override
            public void run() {
                failQueue();
            }
        }).start();
    }

    public void failQueue(){
        while (true){
            MeetingQueueVo meetingQueueVo = QueueUtil.poll();
            if(meetingQueueVo == null){
                // 执行一个休息10分钟
                try {
                    TimeUnit.MINUTES.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            int failNum = meetingQueueVo.getFailNum();
            if(failNum<3){
                failNum++;
                String method = meetingQueueVo.getMethod();
                String param = meetingQueueVo.getParam();
                try {
                    Class cls = SpringUtil.getBean("MeetingHttpServiceImpl").getClass();
                    Method m = cls.getDeclaredMethod(method, String.class);
                    if(method.contains("List")){
                        JSONObject json = JSONObject.parseObject(param);
                        String startTime = json.getString("startTime");
                        String endTime = json.getString("endTime");
                        Object invoke = m.invoke(SpringUtil.getBean("MeetingHttpServiceImpl"), startTime,endTime);
                    }else{
                        JSONObject json = JSONObject.parseObject(param);
                        String id = json.getString("id");
                        Object invoke = m.invoke(SpringUtil.getBean("MeetingHttpServiceImpl"), id);
                    }
                }catch (Exception exception){
                    exception.printStackTrace();
                }
            }else{
                // 记录问题日志
            }
            // 执行一个休息10分钟
            try {
                TimeUnit.MINUTES.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
