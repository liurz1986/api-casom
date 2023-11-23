package com.vrv.vap.apicasom.business.task.service.impl;

import com.vrv.vap.apicasom.business.task.dao.HwMeetingDao;
import com.vrv.vap.apicasom.business.task.service.HwMeetingDataService;
import com.vrv.vap.apicasom.business.task.service.MeetingHttpService;
import com.vrv.vap.apicasom.frameworks.util.MeetingUtil;
import com.vrv.vap.apicasom.frameworks.util.RedisUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import java.util.List;

/**
 * @author: 梁国露
 * @since: 2023/2/16 17:15
 * @description:
 */
@Service("historyHwMeetingDataService")
public class HistoryHwMeetingDataServiceImpl implements HwMeetingDataService {
    private static Logger logger = LoggerFactory.getLogger(HistoryHwMeetingDataServiceImpl.class);
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
        logger.info("历史会议数据同步开始");
        String token = meetingHttpService.getToken(0);
        if(StringUtils.isEmpty(token)){
            logger.error("获取token为空,请确认！");
            return;
        }
        logger.info("token的值："+token);
        MeetingUtil.token= token;
        logger.info("历史会议数据同步:历史会议列表");
        List<String> ids = queryMeetingIds(startTime,endTime);
        if(CollectionUtils.isEmpty(ids)){
            logger.warn("历史会议列表id为空,不处理");
            return;
        }
        logger.info("历史会议数据同步:历史会议数据同步");
        handleMeetingInfo(ids);
        logger.info("历史会议数据同步:历史会议告警信息数据同步");
        handleMeetingAlarm(ids);
    }
    @Override
    public List<String> queryMeetingIds(String startTime, String endTime) {
        List<String> ids = meetingHttpService.getHistoryMeetingList(startTime,endTime,0);
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
        for(String id : ids){
            meetingHttpService.getHistoryMeetingInfo(id,0);
        }
    }

    @Override
    public void handleMeetingAlarm(List<String> ids) {
        // 重新获取token，防止token过期
        String token = meetingHttpService.getToken(0);
        if(StringUtils.isEmpty(token)){
            logger.error("获取token为空,请确认！");
            return;
        }
        logger.info("token的值："+token);
        MeetingUtil.token= token;
        if(CollectionUtils.isNotEmpty(ids)){
            // 删除会议告警信息
            hwMeetingDao.deleteDbData("hw_meeting_alarm",ids);
        }
        for(String id: ids){
            meetingHttpService.getHistoryMeetingAlarm(id,0);
        }
    }
}
