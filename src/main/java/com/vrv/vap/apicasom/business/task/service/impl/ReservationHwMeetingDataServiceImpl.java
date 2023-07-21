package com.vrv.vap.apicasom.business.task.service.impl;

import com.alibaba.fastjson.JSON;
import com.vrv.vap.apicasom.business.task.bean.HwMeetingAlarm;
import com.vrv.vap.apicasom.business.task.dao.HwMeetingDao;
import com.vrv.vap.apicasom.business.task.service.HwMeetingDataService;
import com.vrv.vap.apicasom.business.task.service.MeetingHttpService;
import com.vrv.vap.apicasom.frameworks.util.MeetingUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: 梁国露
 * @since: 2023/2/16 17:16
 * @description:
 */
@Service("reservationHwMeetingDataService")
public class ReservationHwMeetingDataServiceImpl implements HwMeetingDataService {
    private static Logger logger = LoggerFactory.getLogger(ReservationHwMeetingDataServiceImpl.class);
    @Autowired
    private MeetingHttpService meetingHttpService;

    @Autowired
    private HwMeetingDao hwMeetingDao;

    /**
     * 同步数据
     * @param startTime
     * @param endTime
     *
     * 2023-7-21
     */
    @Override
    public void syncData(String startTime,String endTime){
        String token = meetingHttpService.getToken(0);
        if(StringUtils.isEmpty(token)){
            logger.error("获取token为空,请确认！");
            return;
        }
        logger.info("token的值："+token);
        MeetingUtil.token= token;
        List<String> ids =  queryMeetingIds(startTime,endTime);
        if(org.apache.commons.collections.CollectionUtils.isEmpty(ids)){
            logger.warn("获取预约会议调度id为空,不处理");
            return;
        }
        logger.warn("预约会议调度，会议有{}个！",ids.size());
        logger.info("预约会议调度所有会议id："+(JSON.toJSONString(ids)));
        handleMeetingInfo(ids);
        logger.warn("预约会议调度，会议详情保存成功");
        handleMeetingAlarm(ids);
        logger.warn("预约会议调度，会议告警保存成功");
    }
    @Override
    public List<String> queryMeetingIds(String startTime, String endTime) {
        List<String> ids = meetingHttpService.getNowMeetingList(startTime,endTime,0);
        return ids;
    }

    @Override
    public void handleMeetingInfo(List<String> ids) {
        if(CollectionUtils.isNotEmpty(ids)){
            // 删除历史数据
            // 删除会议详情信息
            hwMeetingDao.deleteDbData("hw_meeting_info",ids);
            // 删除会议节点信息
            hwMeetingDao.deleteDbData("hw_meeting_participant",ids);
            // 删除会议与会人信息
            hwMeetingDao.deleteDbData("hw_meeting_attendee",ids);
        }
        for(String id:ids){
            meetingHttpService.getNowMeetingInfo(id,0);
        }
    }

    @Override
    public void handleMeetingAlarm(List<String> ids) {
        if(CollectionUtils.isNotEmpty(ids)){
            // 删除会议告警信息
            hwMeetingDao.deleteDbData("hw_meeting_alarm",ids);
        }
        for(String id:ids){
            meetingHttpService.getNowMeetingAlarm(id,0);
        }
    }
}
