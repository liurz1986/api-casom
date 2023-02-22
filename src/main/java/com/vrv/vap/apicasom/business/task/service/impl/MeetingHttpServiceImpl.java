package com.vrv.vap.apicasom.business.task.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vrv.vap.apicasom.business.task.bean.*;
import com.vrv.vap.apicasom.business.task.bean.hwmeetingbean.*;
import com.vrv.vap.apicasom.business.task.constant.MeetingUrlConstant;
import com.vrv.vap.apicasom.business.task.job.FailQueueJob;
import com.vrv.vap.apicasom.business.task.job.InitTokenJob;
import com.vrv.vap.apicasom.business.task.service.*;
import com.vrv.vap.apicasom.frameworks.util.Base64Utils;
import com.vrv.vap.apicasom.frameworks.util.CronUtil;
import com.vrv.vap.apicasom.frameworks.util.HttpClientUtils;
import com.vrv.vap.apicasom.frameworks.util.QueueUtil;
import com.vrv.vap.jpa.common.UUIDUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author: 梁国露
 * @since: 2023/2/17 13:54
 * @description:
 */
@Service
public class MeetingHttpServiceImpl implements MeetingHttpService {
    // 日志
    private Logger logger = LoggerFactory.getLogger(MeetingHttpServiceImpl.class);

    /**
     * gson对象
     */
    private static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    Map<String, ZkyUnitBean> zkyUnitBeanMap = new ConcurrentHashMap<>();

    public String token = null;

    @Value("${hw.meeting.url}")
    private String url;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private HwMeetingInfoService hwMeetingInfoService;

    @Autowired
    private HwMeetingAttendeeService hwMeetingAttendeeService;

    @Autowired
    private HwMeetingParticipantService hwMeetingParticipantService;

    @Autowired
    private HwMeetingAlarmService hwMeetingAlarmService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ZkyUnitService zkyUnitService;

    /**
     * 获取请求头
     *
     * @return
     */
    public Map<String, String> getHeader() {
        // 初始化城市
        if (zkyUnitBeanMap.isEmpty()) {
            zkyUnitBeanMap = zkyUnitService.initCity();
        }
        Map<String, String> header = new HashMap<>();
        if (StringUtils.isBlank(token)) {
            token = getToken(0);
        }
        header.put("token", token);
        return header;
    }

    /**
     * 获取token
     */
    @Override
    public String getToken(Integer errorNum) {
        String tokenRes = null;
        String configValue = systemConfigService.getSysConfigById("hw_meeting_use");
        JSONObject jsonObject = JSONObject.parseObject(configValue);
        String username = jsonObject.getString("username");
        String password = jsonObject.getString("password");
        String tokenUrl = url + MeetingUrlConstant.TOKEN_URL;
        Map<String, String> header = new HashMap<>();
        String encode = Base64Utils.encodeBase64(username + ":" + password);
        logger.warn("get token base64 encode={}", encode);
        header.put("Authorization", "Basic " + encode);
        try {
            String result = HttpClientUtils.doGet(tokenUrl, null, header);
            Token token = gson.fromJson(result, Token.class);
            tokenRes = token.getUuid();
        } catch (Exception ex) {
            logger.error("get token error,msg={}", ex.getLocalizedMessage());
            MeetingQueueVo meetingQueueVo = new MeetingQueueVo();
            meetingQueueVo.setId(UUIDUtils.get32UUID());
            meetingQueueVo.setMethod("getToken");
            meetingQueueVo.setErrorMsg(ex.getLocalizedMessage());
            meetingQueueVo.setErrorNum(errorNum);
            QueueUtil.put(meetingQueueVo);
        }

        return tokenRes;
    }

    @Override
    public void updateToken(String newToken) {
        token = newToken;
    }

    /**
     * 查询会议列表
     */
    @Override
    public List<String> getHistoryMeetingList(String startTime, String endTime, Integer errorNum) {
        List<String> result = new ArrayList<>();
        Map<String, String> header = getHeader();
        Map<String, String> param = new HashMap<>();
        param.put("startTime", startTime);
        param.put("endTime", endTime);
        String urlStr = url + MeetingUrlConstant.HISTORY_LIST_URL;
        try {
            String res = HttpClientUtils.doPost(urlStr, param, header);
            HistoryList historyList = gson.fromJson(res, HistoryList.class);
            List<Content> contentList = new ArrayList<>();
            if (historyList != null) {
                contentList.addAll(historyList.getContent());
            }

            if (CollectionUtils.isNotEmpty(contentList)) {
                List<String> ids = contentList.stream().map(Content::getId).collect(Collectors.toList());
                result.addAll(ids);
            }
        } catch (Exception ex) {
            logger.error("查询时间段在{}到{}的历史会议记录错误！msg={}", startTime, endTime, ex.getLocalizedMessage());
            MeetingQueueVo meetingQueueVo = new MeetingQueueVo();
            meetingQueueVo.setId(UUIDUtils.get32UUID());
            meetingQueueVo.setMethod("getHistoryMeetingList");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("startTime", startTime);
            jsonObject.put("endTime", endTime);
            meetingQueueVo.setParam(JSONObject.toJSONString(jsonObject));
            meetingQueueVo.setErrorMsg(ex.getLocalizedMessage());
            meetingQueueVo.setErrorNum(errorNum);
            QueueUtil.put(meetingQueueVo);
        }
        return result;
    }

    /**
     * 查询历史会议详情
     */
    @Override
    public void getHistoryMeetingInfo(String id, Integer errorNum) {
        Map<String, String> header = getHeader();
        String infoUrl = url + MeetingUrlConstant.HISTORY_INFO_URL;
        infoUrl = infoUrl.replace("{0}", id);
        try {
            String infoStr = HttpClientUtils.doGet(infoUrl, null, header);
            MeetingInfo meetingInfo = gson.fromJson(infoStr, MeetingInfo.class);
            saveMeetingInfo(meetingInfo);
            saveMeetingAttendee(meetingInfo);
            saveMeetingParticipant(meetingInfo);
        } catch (Exception ex) {
            logger.error("获取历史会议-会议ID为{}的会议详情失败！msg={}", id, ex.getLocalizedMessage());
            MeetingQueueVo meetingQueueVo = new MeetingQueueVo();
            meetingQueueVo.setId(UUIDUtils.get32UUID());
            meetingQueueVo.setMethod("getHistoryMeetingInfo");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", id);
            meetingQueueVo.setParam(JSONObject.toJSONString(jsonObject));
            meetingQueueVo.setErrorMsg(ex.getLocalizedMessage());
            meetingQueueVo.setErrorNum(errorNum);
            QueueUtil.put(meetingQueueVo);
        }
    }

    /**
     * 保存会议详细信息
     *
     * @param meetingInfo
     */
    public void saveMeetingInfo(MeetingInfo meetingInfo) {
        HwMeetingInfo hwMeetingInfo = new HwMeetingInfo();
        hwMeetingInfo.setMeetingId(meetingInfo.getId());
        hwMeetingInfo.setDuration(meetingInfo.getDuration());
        List<String> organizationNames = new ArrayList<>();
        List<ParticipantRsp> participants = meetingInfo.getParticipants();
        List<AttendeeRsp> attendees = meetingInfo.getAttendees();
        if (participants != null) {
            List<String> participantsNames = participants.stream().map(ParticipantRsp::getOrganizationName).collect(Collectors.toList());
            organizationNames.addAll(participantsNames);
        }
        if (attendees != null) {
            List<String> attendeesNames = attendees.stream().map(AttendeeRsp::getOrganizationName).collect(Collectors.toList());
            organizationNames.addAll(attendeesNames);
        }
        organizationNames = organizationNames.stream().distinct().collect(Collectors.toList());
        hwMeetingInfo.setOrganizationName(String.join(",", organizationNames));
        hwMeetingInfo.setScheduleEndTime(CronUtil.utcToLocal(meetingInfo.getScheduleEndTime()));
        hwMeetingInfo.setScheduleStartTime(CronUtil.utcToLocal(meetingInfo.getScheduleStartTime()));
        List<String> participantss = participants.stream().map(ParticipantRsp::getName).collect(Collectors.toList());
        hwMeetingInfo.setParticipantUnity(String.join(",", participantss));
        hwMeetingInfo.setAttendeeCount(meetingInfo.getAttendees() == null ? 0 : meetingInfo.getAttendees().size());
        hwMeetingInfo.setParticipantCount(meetingInfo.getParticipants() == null ? 0 : meetingInfo.getParticipants().size());
        hwMeetingInfo.setStage("OFFLINE");
        hwMeetingInfo.setOutService(pointIsOutService(meetingInfo));
        hwMeetingInfoService.save(hwMeetingInfo);
    }

    /**
     * 判断与会者人员是否在节点组织内
     * @param meetingInfo
     * @return
     */
    public String pointIsOutService(MeetingInfo meetingInfo){
        String pointName = meetingInfo.getOrganizationName();
        List<AttendeeRsp> attendees = meetingInfo.getAttendees();
        if(CollectionUtils.isNotEmpty(attendees)){
            List<String> attendeesNames = attendees.stream().map(AttendeeRsp::getOrganizationName).collect(Collectors.toList());
            for(String organizationName : attendeesNames){
                if(!organizationName.equals(pointName)){
                    return "1";
                }
            }
        }
        return "0";
    }

    /**
     * 保存会议与会人信息
     *
     * @param meetingInfo
     */
    public void saveMeetingAttendee(MeetingInfo meetingInfo) {
        clearHistoryData("hw_meeting_attendee", meetingInfo.getId());
        List<HwMeetingAttendee> hwMeetingAttendees = new ArrayList<>();
        List<AttendeeRsp> attendees = meetingInfo.getAttendees();
        if (attendees != null) {
            Map<String, List<AttendeeRsp>> attendeeMap = attendees.stream().collect(Collectors.groupingBy(AttendeeRsp::getParticipantName));
            for (Map.Entry<String, List<AttendeeRsp>> entry : attendeeMap.entrySet()) {
                HwMeetingAttendee hwMeetingAttendee = new HwMeetingAttendee();
                hwMeetingAttendee.setId(UUIDUtils.get32UUID());

                hwMeetingAttendee.setMeetingId(meetingInfo.getId());
                hwMeetingAttendee.setDuration(meetingInfo.getDuration());
                hwMeetingAttendee.setParticipantName(entry.getKey());
                hwMeetingAttendee.setUserCount(entry.getValue().size());
                hwMeetingAttendee.setBranch(zkyUnitBeanMap.get(entry.getKey()).getBranch());
                hwMeetingAttendee.setCity(zkyUnitBeanMap.get(entry.getKey()).getCity());
                hwMeetingAttendees.add(hwMeetingAttendee);
            }
            hwMeetingAttendeeService.save(hwMeetingAttendees);
        }
        logger.warn("保存会议《{}》与会人信息完成，与会人个数{}", meetingInfo.getId(), hwMeetingAttendees.size());
    }

    /**
     * 保存会议节点信息
     *
     * @param meetingInfo
     */
    public void saveMeetingParticipant(MeetingInfo meetingInfo) {
        clearHistoryData("hw_meeting_participant", meetingInfo.getId());
        List<ParticipantRsp> participants = meetingInfo.getParticipants();
        List<HwMeetingParticipant> list = new ArrayList<>();
        if (participants != null) {
            for (ParticipantRsp participantRsp : participants) {
                HwMeetingParticipant participant = new HwMeetingParticipant();
                participant.setId(UUIDUtils.get32UUID());
                participant.setName(participantRsp.getName());
                participant.setDuration(meetingInfo.getDuration());
                participant.setMeetingId(meetingInfo.getId());
                participant.setCity(zkyUnitBeanMap.get(participantRsp.getOrganizationName()).getCity());
                participant.setBranch(zkyUnitBeanMap.get(participantRsp.getOrganizationName()).getBranch());
                participant.setStage("OFFLINE");
                participant.setOrganizationName(participantRsp.getOrganizationName());
                participant.setScheduleEndTime(CronUtil.utcToLocal(meetingInfo.getScheduleEndTime()));
                participant.setScheduleStartTime(CronUtil.utcToLocal(meetingInfo.getScheduleStartTime()));
                participant.setTerminalType(participantRsp.getTerminalType());
                list.add(participant);
            }
            hwMeetingParticipantService.save(list);
        }
        logger.warn("保存会议《{}》节点信息完成，节点个数{}", meetingInfo.getId(), list.size());
    }

    /**
     * 查询历史会议告警
     *
     * @param id
     */
    @Override
    public void getHistoryMeetingAlarm(String id, Integer errorNum) {
        Map<String, String> header = getHeader();
        String urlStr = url + MeetingUrlConstant.HISTORY_ALARM_URL;
        urlStr = urlStr.replace("{0}", id);
        try {
            String res = HttpClientUtils.doPost(urlStr, null, header);
            AlarmResBean alarmResBean = gson.fromJson(res, AlarmResBean.class);
            List<AlarmVo> contentList = alarmResBean.getContent();
            List<HwMeetingAlarm> alarms = new ArrayList<>();

            for (AlarmVo content : contentList) {
                HwMeetingAlarm alarm = new HwMeetingAlarm();
                alarm.setId(UUIDUtils.get32UUID());
                alarm.setMeetingId(id);
                alarm.setName(content.getName());
                alarm.setAlarmNo(content.getAlarmNo());
                alarm.setAlarmTime(CronUtil.utcToLocal(content.getAlarmTime()));
                alarm.setAlarmType(content.getAlarmType());
                alarm.setClearedTime(CronUtil.utcToLocal(content.getClearedTime()));
                alarm.setSeverity(content.getSeverity());
                alarms.add(alarm);
            }
            hwMeetingAlarmService.save(alarms);
        } catch (Exception ex) {
            logger.error("保存历史会议-会议ID{}的告警信息失败！信息为={}", id, ex.getLocalizedMessage());
            MeetingQueueVo meetingQueueVo = new MeetingQueueVo();
            meetingQueueVo.setId(UUIDUtils.get32UUID());
            meetingQueueVo.setMethod("getHistoryMeetingAlarm");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", id);
            meetingQueueVo.setParam(JSONObject.toJSONString(jsonObject));
            meetingQueueVo.setErrorMsg(ex.getLocalizedMessage());
            meetingQueueVo.setErrorNum(errorNum);
            QueueUtil.put(meetingQueueVo);
        }
    }

    /**
     * 查询预约会议列表
     */
    @Override
    public List<String> getNowMeetingList(String startTime, String endTime, Integer errorNum) {
        List<String> ids = new ArrayList<>();
        Map<String, String> header = getHeader();
        Map<String, String> param = new HashMap<>();
        param.put("startTime", startTime);
        param.put("endTime", endTime);

        String nowMeetingUrl = url + MeetingUrlConstant.NOW_LIST_URL + "";
        try {
            String res = HttpClientUtils.doPost(nowMeetingUrl, param, header);
            NowMeetingList meetingList = gson.fromJson(res, NowMeetingList.class);
            List<ScheduleConfBrief> list = meetingList.getContent();
            if (CollectionUtils.isNotEmpty(list)) {
                List<String> confIds = list.stream().map(ScheduleConfBrief::getId).collect(Collectors.toList());
                ids.addAll(confIds);
            }
        } catch (Exception ex) {
            logger.error("查询预约会议列表失败，msg={}", ex.getLocalizedMessage());
            MeetingQueueVo meetingQueueVo = new MeetingQueueVo();
            meetingQueueVo.setId(UUIDUtils.get32UUID());
            meetingQueueVo.setMethod("getNowMeetingList");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("startTime", startTime);
            jsonObject.put("endTime", endTime);
            meetingQueueVo.setParam(JSONObject.toJSONString(jsonObject));
            meetingQueueVo.setErrorMsg(ex.getLocalizedMessage());
            meetingQueueVo.setErrorNum(errorNum);
            QueueUtil.put(meetingQueueVo);
        }

        return ids;
    }

    /**
     * 查询预约会议详情
     *
     * @param id
     */
    @Override
    public void getNowMeetingInfo(String id, Integer errorNum) {
        HwMeetingInfo hwMeetingInfo = new HwMeetingInfo();
        Map<String, String> header = getHeader();
        Map<String, Object> param = new HashMap<>();
        param.put("active", true);
        String nowMeetingInfoUrl = url + MeetingUrlConstant.NOW_INFO_URL;
        nowMeetingInfoUrl = nowMeetingInfoUrl.replace("{0}", id);
        try {
            String res = HttpClientUtils.doGet(nowMeetingInfoUrl, null, header);
            ConferenceRspVo conferenceRspVo = gson.fromJson(res, ConferenceRspVo.class);
            ConferenceRsp conferenceRsp = conferenceRspVo.getConference();
            hwMeetingInfo.setMeetingId(conferenceRsp.getId());
            hwMeetingInfo.setStage(conferenceRsp.getStage());
            hwMeetingInfo.setScheduleStartTime(CronUtil.utcToLocal(conferenceRsp.getScheduleStartTime()));
            hwMeetingInfo.setDuration(conferenceRsp.getDuration());
            hwMeetingInfo.setOrganizationName(conferenceRsp.getOrganizationName());
            hwMeetingInfo.setOutService("0");
            hwMeetingInfoService.save(hwMeetingInfo);
            logger.warn("保存现有会议信息成功！会议ID={}", id);
            getNowMeetingParticipants(conferenceRsp.getId(), conferenceRsp.getOrganizationName(), conferenceRsp.getDuration(), CronUtil.utcToLocal(conferenceRsp.getScheduleStartTime()));
        } catch (Exception ex) {
            MeetingQueueVo meetingQueueVo = new MeetingQueueVo();
            meetingQueueVo.setId(UUIDUtils.get32UUID());
            meetingQueueVo.setMethod("getNowMeetingInfo");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", id);
            meetingQueueVo.setParam(JSONObject.toJSONString(jsonObject));
            meetingQueueVo.setErrorMsg(ex.getLocalizedMessage());
            meetingQueueVo.setErrorNum(errorNum);
            QueueUtil.put(meetingQueueVo);
        }
    }

    /**
     * 查询会场信息
     *
     * @param id
     */
    @Override
    public void getNowMeetingParticipants(String id, String organizationName, int duration, Date scheduleStartTime) {
        clearHistoryData("hw_meeting_participant", id);
        Map<String, String> header = getHeader();
        String nowMeetingParticipant = url + MeetingUrlConstant.NOW_PARTICIPANT_URL;
        nowMeetingParticipant = nowMeetingParticipant.replace("{0}", id);

        try {
            String res = HttpClientUtils.doPost(nowMeetingParticipant, null, header);
            OnlineConferencesRes onlineConferencesRes = gson.fromJson(res, OnlineConferencesRes.class);
            List<ParticipantDetail> content = onlineConferencesRes.getContent();
            if (CollectionUtils.isNotEmpty(content)) {
                List<HwMeetingParticipant> hwMeetingParticipants = new ArrayList<>();
                for (ParticipantDetail participantDetail : content) {
                    if (participantDetail.getState().isOnline()) {
                        HwMeetingParticipant hwMeetingParticipant = new HwMeetingParticipant();
                        hwMeetingParticipant.setId(UUIDUtils.get32UUID());
                        hwMeetingParticipant.setMeetingId(id);
                        hwMeetingParticipant.setName(participantDetail.getGeneralParam().getName());
                        hwMeetingParticipant.setTerminalType(participantDetail.getGeneralParam().getModel());
                        hwMeetingParticipant.setStage("ONLINE");
                        hwMeetingParticipant.setBranch(zkyUnitBeanMap.get(organizationName).getBranch());
                        hwMeetingParticipant.setCity(zkyUnitBeanMap.get(organizationName).getCity());
                        hwMeetingParticipant.setOrganizationName(organizationName);
                        hwMeetingParticipant.setDuration(duration);
                        hwMeetingParticipant.setScheduleStartTime(scheduleStartTime);
                        hwMeetingParticipants.add(hwMeetingParticipant);
                    }
                }
                hwMeetingParticipantService.save(hwMeetingParticipants);
            }

        } catch (Exception ex) {
            logger.error("保存现有会议节点信息失败，msg={}", ex.getLocalizedMessage());
            throw new RuntimeException(ex);
        }

    }

    @Override
    public void updateCity(Map<String, ZkyUnitBean> map) {
        zkyUnitBeanMap.clear();
        for (Map.Entry<String, ZkyUnitBean> entry : map.entrySet()) {
            zkyUnitBeanMap.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public int initMeetingRooms(){
        Map<String,String> header = getHeader();
        String roomUrl = url+MeetingUrlConstant.MEETING_ROOMS_URL;
        String res = HttpClientUtils.doPost(roomUrl, null, header);
        if(StringUtils.isNotBlank(res)){
            MeetingRoomsRes meetingRoomsRes = gson.fromJson(res,MeetingRoomsRes.class);
            int total = meetingRoomsRes.getTotalElements();
            return total;
        }
        return 0;
    }

    /**
     * 清除历史数据
     *
     * @param tableName
     * @param meetingId
     */
    public void clearHistoryData(String tableName, String meetingId) {
        String sql = "delete from " + tableName + " where meeting_id = '" + meetingId + "';";
        jdbcTemplate.execute(sql);
    }
}

