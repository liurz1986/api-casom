package com.vrv.vap.apicasom.business.task.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vrv.vap.apicasom.business.task.bean.*;
import com.vrv.vap.apicasom.business.task.bean.httpres.hwmeetingbean.*;
import com.vrv.vap.apicasom.business.task.constant.MeetingUrlConstant;
import com.vrv.vap.apicasom.business.task.service.*;
import com.vrv.vap.apicasom.frameworks.util.*;
import com.vrv.vap.jpa.common.UUIDUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
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

    @Value("${hw.meeting.url}")
    private String url;

    @Value("${hw.meeting.organizationId}")
    private String organizationId;

    @Value("${hw.meeting.sys.username}")
    private String sysUserName;

    @Value("${hw.meeting.sys.password}")
    private String sysPassword;

    @Value("${hw.meeting.sys.register.hd}")
    private String registerHd;

    @Value("${hw.meeting.sys.register.sn}")
    private String registerSn;

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
    private ZkyUnitService zkyUnitService;

    private static Map<String,ZkyUnitBean> zkyUnitBeanMap = new HashMap<>();

    private static String token;

    /**
     * 获取请求头
     *
     * @return
     */
    public Map<String, String> getHeader() {
        // 初始化城市
        Map<String,ZkyUnitBean> zkyUnitMap = HwMeetingServiceImpl.zkyUnitBeanMap;
        if (zkyUnitBeanMap.isEmpty() && zkyUnitMap.isEmpty()) {
            zkyUnitBeanMap = zkyUnitService.initCity();
        }else if(zkyUnitBeanMap.isEmpty() && !zkyUnitMap.isEmpty()){
            zkyUnitBeanMap = zkyUnitMap;
        }
        Map<String, String> header = new HashMap<>();
        header.put("token", MeetingUtil.token);
        header.put("Content-type","application/json;charset=UTF-8");
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
        if(jsonObject == null){
            logger.warn("配置项hw_meeting_use未配置！");
            return tokenRes;
        }
        String username = jsonObject.getString("username");
        String password = jsonObject.getString("password");
        String tokenUrl = url +"/conf-portal" + MeetingUrlConstant.TOKEN_URL;
        Map<String, String> header = new HashMap<>();
        String encode = Base64Utils.encodeBase64(username + ":" + password);
        logger.warn("获取token的用户名:"+username+"  ,,密码: "+password);
        logger.warn("get token base64 encode={}", encode);
        header.put("Authorization", "Basic " + encode);
        header.put("Content-type","application/json;charset=UTF-8");
        try {
            String result = HttpClientUtils.doGet(tokenUrl, null, header);
            logger.warn("getToken 接口返回：{}",result);
            Token token = gson.fromJson(result, Token.class);
            tokenRes = token.getUuid();
        } catch (Exception ex) {
            logger.error("get token error,msg={}", ex);
            throw new RuntimeException("获取token异常");
        }
        return tokenRes;
    }

    /**
     * 查询会议列表
     */
    @Override
    public List<String> getHistoryMeetingList(String startTime, String endTime, Integer errorNum) {
        List<String> result = new ArrayList<>();
        Map<String, String> header = getHeader();
        Map<String, Object> param = new HashMap<>();
        param.put("startTime", startTime+" UTC");
        param.put("endTime", endTime+" UTC");
        String urlStr = url+"/conf-portal" + MeetingUrlConstant.HISTORY_LIST_URL;
        urlStr=  urlStr.replace("{page}","0");
        try {
            String res = HttpClientUtils.doPost(urlStr, param, header);
            logger.warn("getHistoryMeetingList 接口返回：{}",res);
            HistoryList historyList = gson.fromJson(res, HistoryList.class);
            List<String> ids = getMettingIds(historyList);
            if(CollectionUtils.isNotEmpty(ids)){
                result.addAll(ids);
            }
            int total = historyList.getTotalElements();
            logger.info("历史会议列表总记录数："+total);
            // 对于查询数据超过当前页的情况,重复调用获取所有数据 2023-08-03
            if(MeetingUrlConstant.size >= total ){
                return result;
            }
            int page = total/MeetingUrlConstant.size;
            if(total >(page*MeetingUrlConstant.size)){
                page = page+1;
            }
            logger.info("历史会议列表总记录数大于每页数量时处理，page为"+page);
            for(int i = 1;i <= page;i++){
                urlStr = url+"/conf-portal" + MeetingUrlConstant.HISTORY_LIST_URL;
                urlStr =  urlStr.replace("{page}",i+"");
                res = HttpClientUtils.doPost(urlStr, param, header);
                logger.warn("getHistoryMeetingList 接口返回：{}",res);
                HistoryList historyListRe = gson.fromJson(res, HistoryList.class);
                List<String> idsRe = getMettingIds(historyListRe);
                if(CollectionUtils.isNotEmpty(idsRe)){
                    result.addAll(idsRe);
                }
            }
        } catch (Exception ex) {
            logger.error("查询时间段在{}到{}的历史会议记录错误！msg={}", startTime, endTime, ex);
            throw new RuntimeException("查询历史会议列表异常");
        }
        return result;
    }

    private List<String> getMettingIds(HistoryList historyList) {
        List<Content> contentList = new ArrayList<>();
        if (historyList != null) {
            contentList.addAll(historyList.getContent());
        }
        if (CollectionUtils.isNotEmpty(contentList)) {
            List<String> ids = contentList.stream().map(Content::getId).collect(Collectors.toList());
            return ids;
        }
        return new ArrayList<>();
    }

    /**
     * 查询历史会议详情
     */
    @Override
    public void getHistoryMeetingInfo(String id, Integer errorNum) {
        Map<String, String> header = getHeader();
        String infoUrl = url+"/conf-portal" + MeetingUrlConstant.HISTORY_INFO_URL;
        infoUrl = infoUrl.replace("{0}", id);
        try {
            String infoStr = HttpClientUtils.doGet(infoUrl, null, header);
            logger.warn("getHistoryMeetingInfo 接口返回：{}",infoStr);
            MeetingInfo meetingInfo = gson.fromJson(infoStr, MeetingInfo.class);
            saveMeetingInfo(meetingInfo);
            logger.warn("历史会议{}详情保存成功！",id);
            saveMeetingAttendee(meetingInfo);
            logger.warn("历史会议{}与会人保存成功！",id);
            saveMeetingParticipant(meetingInfo);
            logger.warn("历史会议{}会议节点保存成功！",id);
        } catch (Exception ex) {
            logger.error("获取历史会议-会议ID为{}的会议详情失败！msg={}", id, ex);
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
        hwMeetingInfoService.save(hwMeetingInfo);
    }

    /**
     * 判断与会者人员是否在节点组织内
     * @param meetingInfo
     * @return
     */
    public String pointIsOutService(MeetingInfo meetingInfo){
        List<ParticipantRsp> participantRsps = meetingInfo.getParticipants();
        List<AttendeeRsp> attendees = meetingInfo.getAttendees();
        if(CollectionUtils.isNotEmpty(attendees)){
            if(CollectionUtils.isNotEmpty(participantRsps)){
                List<String> attendeesNames = attendees.stream().map(AttendeeRsp::getOrganizationName).collect(Collectors.toList());
                List<String> participantName = participantRsps.stream().map(ParticipantRsp::getOrganizationName).collect(Collectors.toList());
                List<String> cj= attendeesNames.stream().filter(item -> !participantName.contains(item)).collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(cj)){
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
        List<HwMeetingAttendee> hwMeetingAttendees = new ArrayList<>();
        List<AttendeeRsp> attendees = meetingInfo.getAttendees();
        List<ParticipantRsp> participantRsps = meetingInfo.getParticipants();
        if (attendees != null) {
            Map<String, List<AttendeeRsp>> attendeeMap = attendees.stream().collect(Collectors.groupingBy(AttendeeRsp::getParticipantName));
            for (Map.Entry<String, List<AttendeeRsp>> entry : attendeeMap.entrySet()) {
                HwMeetingAttendee hwMeetingAttendee = new HwMeetingAttendee();
                hwMeetingAttendee.setId(UUIDUtils.get32UUID());
                //TODO
                // 通过与会人列表中会场名称与会议详情信息中会场信息的会场名称匹配得到会场ID
                List<ParticipantRsp> participantRspList = participantRsps.stream().filter(item->item.getName().equals(entry.getKey())).collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(participantRspList)){
                    ParticipantRsp participantRsp = participantRspList.get(0);
                    hwMeetingAttendee.setParticipantCode(participantRsp.getId());
                }
                hwMeetingAttendee.setMeetingId(meetingInfo.getId());
                hwMeetingAttendee.setDuration(meetingInfo.getDuration());
                hwMeetingAttendee.setParticipantName(entry.getKey());
                hwMeetingAttendee.setUserCount(entry.getValue().size());
                ZkyUnitBean bean=  zkyUnitBeanMap.get(entry.getKey());
                if(null == bean){
                    logger.error(entry.getKey()+"在表zky_unit没有配置");
                }else{
                    hwMeetingAttendee.setBranch(zkyUnitBeanMap.get(entry.getKey()).getBranch());
                    hwMeetingAttendee.setCity(zkyUnitBeanMap.get(entry.getKey()).getCity());
                }
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
        List<ParticipantRsp> participants = meetingInfo.getParticipants();
        List<HwMeetingParticipant> list = new ArrayList<>();
        if (participants != null) {
            for (ParticipantRsp participantRsp : participants) {
                HwMeetingParticipant participant = new HwMeetingParticipant();
                participant.setId(UUIDUtils.get32UUID());
                participant.setName(participantRsp.getName());
                participant.setDuration(meetingInfo.getDuration());
                participant.setMeetingId(meetingInfo.getId());
                participant.setParticipantCode(participantRsp.getId());
                ZkyUnitBean bean=  zkyUnitBeanMap.get(participantRsp.getOrganizationName());
                if(null == bean){
                    logger.error(participantRsp.getOrganizationName()+"在表zky_unit没有配置");
                }else{
                    participant.setCity(zkyUnitBeanMap.get(participantRsp.getOrganizationName()).getCity());
                    participant.setBranch(zkyUnitBeanMap.get(participantRsp.getOrganizationName()).getBranch());
                }
                participant.setStage("OFFLINE");
                participant.setOrganizationName(participantRsp.getOrganizationName());
                participant.setScheduleEndTime(CronUtil.utcToLocal(meetingInfo.getScheduleEndTime()));
                participant.setScheduleStartTime(CronUtil.utcToLocal(meetingInfo.getScheduleStartTime()));
                participant.setTerminalType(participantRsp.getTerminalType());
                participant.setOutService(pointIsOutService(meetingInfo));
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
        List<AlarmVo> result = new ArrayList<>();
        Map<String, String> header = getHeader();
        String urlStr = url+"/conf-portal" + MeetingUrlConstant.HISTORY_ALARM_URL;
        urlStr = urlStr.replace("{0}", id).replace("{page}", "0");
        try {
            Map<String,Object> param = new HashMap<>();
            String res = HttpClientUtils.doPost(urlStr, param, header);
            logger.warn("getHistoryMeetingAlarm 接口返回：{}",res);
            AlarmResBean alarmResBean = gson.fromJson(res, AlarmResBean.class);
            List<AlarmVo> contentList = alarmResBean.getContent();
            if(CollectionUtils.isNotEmpty(contentList)){
                result.addAll(contentList);
            }
           // 对于查询数据超过当前页的情况,重复调用获取所有数据 2023-08-03
            int total = alarmResBean.getTotalElements();
            if(MeetingUrlConstant.size < total ){
                int page = total/MeetingUrlConstant.size;
                if(total >(page*MeetingUrlConstant.size)){
                    page = page+1;
                }
                logger.info("历史会议告警总记录数大于每页数量时处理，page为"+page);
                for(int i = 1;i <= page;i++){
                    urlStr = url+"/conf-portal" + MeetingUrlConstant.HISTORY_ALARM_URL;
                    urlStr =  urlStr.replace("{0}", id).replace("{page}",i+"");
                    res = HttpClientUtils.doPost(urlStr, param, header);
                    logger.warn("getHistoryMeetingAlarm 接口返回：{}",res);
                    AlarmResBean alarmResBeanRes = gson.fromJson(res, AlarmResBean.class);
                    List<AlarmVo> contentListRes = alarmResBeanRes.getContent();
                    if(CollectionUtils.isNotEmpty(contentListRes)){
                        result.addAll(contentListRes);
                    }
                }
            }
            saveHistMeetingAlarm(contentList,id);
            logger.warn("保存历史会议-会议ID{}的告警信息成功！",id);
        } catch (Exception ex) {
            logger.error("保存历史会议-会议ID{}的告警信息失败！信息为={}", id, ex);
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

    private void saveHistMeetingAlarm(List<AlarmVo> contentList,String id) {
        List<HwMeetingAlarm> alarms = new ArrayList<>();
        for (AlarmVo content : contentList) {
            HwMeetingAlarm alarm = new HwMeetingAlarm();
            alarm.setId(UUIDUtils.get32UUID());
            alarm.setMeetingId(id);
            alarm.setName(content.getName());
            alarm.setAlarmNo(content.getAlarmNo());
            alarm.setAlarmTime(CronUtil.utcToLocal(content.getAlarmTime()));
            alarm.setAlarmType(content.getName());
            alarm.setClearedTime(CronUtil.utcToLocal(content.getClearedTime()));
            alarm.setSeverity(content.getSeverity());
            alarm.setAlarmStatus("history");
            alarms.add(alarm);
        }
        hwMeetingAlarmService.save(alarms);
    }

    /**
     * 查询预约会议列表
     */
    @Override
    public List<String> getNowMeetingList(String startTime, String endTime, Integer errorNum) {
        List<String> result = new ArrayList<>();
        Map<String, String> header = getHeader();
        Map<String, Object> param = new HashMap<>();
        param.put("startTime", startTime+" UTC");
        param.put("endTime", endTime+" UTC");
        param.put("active",true);
        String nowMeetingUrl = url+"/conf-portal" + MeetingUrlConstant.NOW_LIST_URL + "";
        nowMeetingUrl = nowMeetingUrl.replace("{page}","0");
        try {
            String res = HttpClientUtils.doPost(nowMeetingUrl, param, header);
            logger.warn("getNowMeetingList 接口返回：{}",res);
            NowMeetingList meetingList = gson.fromJson(res, NowMeetingList.class);
            List<String> ids = getNowMeetingIds(meetingList);
            if(CollectionUtils.isNotEmpty(ids)){
                result.addAll(ids);
            }
            // 对于查询数据超过当前页的情况,重复调用获取所有数据 2023-08-03
            int total = meetingList.getTotalElements();
            logger.info("查询预约会议列表总记录数："+total);
            if(MeetingUrlConstant.size >= total ){
                return result;
            }
            int page = total/MeetingUrlConstant.size;
            if(total >(page*MeetingUrlConstant.size)){
                page = page+1;
            }
            logger.info("已预约会议列表总记录数大于每页数量时处理，page为"+page);
            for(int i = 1;i <= page;i++){
                nowMeetingUrl = url+"/conf-portal" + MeetingUrlConstant.NOW_LIST_URL + "";
                nowMeetingUrl =  nowMeetingUrl.replace("{page}",i+"");
                res = HttpClientUtils.doPost(nowMeetingUrl, param, header);
                logger.warn("getNowMeetingList 接口返回：{}",res);
                NowMeetingList meetingListRes = gson.fromJson(res, NowMeetingList.class);
                List<String> idsRe = getNowMeetingIds(meetingListRes);
                if(CollectionUtils.isNotEmpty(idsRe)){
                    result.addAll(idsRe);
                }
            }
            return result;
        } catch (Exception ex) {
            logger.error("查询预约会议列表失败，msg={}", ex);
            throw new RuntimeException("查询预约会议列表异常");
        }
    }

    private List<String> getNowMeetingIds(NowMeetingList meetingList) {
        List<ScheduleConfBrief> list = meetingList.getContent();
        if (CollectionUtils.isNotEmpty(list)) {
            List<String> confIds = list.stream().map(ScheduleConfBrief::getId).collect(Collectors.toList());
            return confIds;
        }
        return new ArrayList<>();
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
        String nowMeetingInfoUrl = url+"/conf-portal" + MeetingUrlConstant.NOW_INFO_URL;
        nowMeetingInfoUrl = nowMeetingInfoUrl.replace("{0}", id);
        try {
            String res = HttpClientUtils.doGet(nowMeetingInfoUrl, null, header);
            logger.warn("getNowMeetingInfo 接口返回：{}",res);
            ConferenceRspVo conferenceRspVo = gson.fromJson(res, ConferenceRspVo.class);
            ConferenceRsp conferenceRsp = conferenceRspVo.getConference();
            String stage = conferenceRsp.getStage();
            hwMeetingInfo.setMeetingId(conferenceRsp.getId());
            hwMeetingInfo.setStage(conferenceRsp.getStage());
            hwMeetingInfo.setScheduleStartTime(CronUtil.utcToLocal(conferenceRsp.getScheduleStartTime()));
            hwMeetingInfo.setDuration(conferenceRsp.getDuration());
            hwMeetingInfo.setOrganizationName(conferenceRsp.getOrganizationName());
            hwMeetingInfoService.save(hwMeetingInfo);
            logger.warn("保存现有会议信息成功！会议ID={}", id);
            // stage为ONLINE才会有数据 2023-08-08
            if(stage.equals("ONLINE")){
                getNowMeetingParticipants(conferenceRsp.getId(), conferenceRsp.getOrganizationName(), conferenceRsp.getDuration(), CronUtil.utcToLocal(conferenceRsp.getScheduleStartTime()));
            }
            logger.warn("保存现有会议节点信息成功！");
        } catch (Exception ex) {
            logger.error("查询预约会议详情异常：{}",ex);
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
        List<ParticipantDetail> list = new ArrayList<>();
        Map<String, String> header = getHeader();
        String nowMeetingParticipant = url+"/conf-portal" + MeetingUrlConstant.NOW_PARTICIPANT_URL;
        nowMeetingParticipant = nowMeetingParticipant.replace("{0}", id).replace("{page}","0");
        Map<String,Object> param = new HashMap<>();
        try {
            String res = HttpClientUtils.doPost(nowMeetingParticipant, param, header);
            logger.warn("getNowMeetingParticipants 接口返回：{}",res);
            OnlineConferencesRes onlineConferencesRes = gson.fromJson(res, OnlineConferencesRes.class);
            List<ParticipantDetail> content = onlineConferencesRes.getContent();
            if(CollectionUtils.isNotEmpty(content)){
                list.addAll(content);
            }
            // 对于查询数据超过当前页的情况,重复调用获取所有数据 2023-08-03
            int total = onlineConferencesRes.getTotalElements();
            logger.info("查询预约会议列表总记录数："+total);
            if(MeetingUrlConstant.size < total ){
                int page = total/MeetingUrlConstant.size;
                if(total >(page*MeetingUrlConstant.size)){
                    page = page+1;
                }
                logger.info("查询会场信息总记录数大于每页数量时处理，page为"+page);
                for(int i= 1;i <= page;i++){
                    nowMeetingParticipant = url+"/conf-portal" + MeetingUrlConstant.NOW_PARTICIPANT_URL;
                    nowMeetingParticipant = nowMeetingParticipant.replace("{0}", id).replace("{page}",i+"");
                    res = HttpClientUtils.doPost(nowMeetingParticipant, param, header);
                    logger.warn("getNowMeetingParticipants 接口返回：{}",res);
                    OnlineConferencesRes onlineConferencesResRe = gson.fromJson(res, OnlineConferencesRes.class);
                    List<ParticipantDetail> contentRe = onlineConferencesResRe.getContent();
                    if(CollectionUtils.isNotEmpty(contentRe)){
                        list.addAll(contentRe);
                    }
                }
            }
            saveNowMeetingParticipants(list,id,organizationName,duration,scheduleStartTime);
        } catch (Exception ex) {
            logger.error("保存现有会议节点信息失败，msg={}", ex);
            throw new RuntimeException(ex);
        }

    }

    private void saveNowMeetingParticipants(List<ParticipantDetail> content,String id, String organizationName, int duration, Date scheduleStartTime) {
        if (CollectionUtils.isNotEmpty(content)) {
            List<HwMeetingParticipant> hwMeetingParticipants = new ArrayList<>();
            for (ParticipantDetail participantDetail : content) {
                if (participantDetail.getState().isOnline()) {
                    HwMeetingParticipant hwMeetingParticipant = new HwMeetingParticipant();
                    hwMeetingParticipant.setId(UUIDUtils.get32UUID());
                    hwMeetingParticipant.setMeetingId(id);
                    hwMeetingParticipant.setParticipantCode(participantDetail.getGeneralParam().getId());
                    hwMeetingParticipant.setName(participantDetail.getGeneralParam().getName());
                    hwMeetingParticipant.setTerminalType(participantDetail.getGeneralParam().getModel());
                    hwMeetingParticipant.setStage("ONLINE");
                    ZkyUnitBean bean=  zkyUnitBeanMap.get(organizationName);
                    if(null == bean){
                        logger.error(organizationName+"在表zky_unit没有配置");
                    }else{
                        hwMeetingParticipant.setBranch(zkyUnitBeanMap.get(organizationName).getBranch());
                        hwMeetingParticipant.setCity(zkyUnitBeanMap.get(organizationName).getCity());
                    }
                    hwMeetingParticipant.setOrganizationName(organizationName);
                    hwMeetingParticipant.setDuration(duration);
                    hwMeetingParticipant.setScheduleStartTime(scheduleStartTime);
                    hwMeetingParticipant.setOutService("0");
                    hwMeetingParticipants.add(hwMeetingParticipant);
                }
            }
            hwMeetingParticipantService.save(hwMeetingParticipants);
        }
    }

    @Override
    public void getNowMeetingAlarm(String id, Integer errorNum) {
        List<AlarmVo> result = new ArrayList<>();
        Map<String, String> header = getHeader();
        String urlStr = url+"/conf-portal" + MeetingUrlConstant.NOW_ALARM_URL;
        urlStr = urlStr.replace("{0}", id).replace("{page}","0");
        try {
            String res = HttpClientUtils.doGet(urlStr, null, header);
            logger.warn("getNowMeetingAlarm 接口返回：{}",res);
            AlarmResBean alarmResBean = gson.fromJson(res, AlarmResBean.class);
            List<AlarmVo> contentList = alarmResBean.getContent();
            if(CollectionUtils.isNotEmpty(contentList)){
                result.addAll(contentList);
            }
            // 对于查询数据超过当前页的情况,重复调用获取所有数据 2023-08-03
            int total = alarmResBean.getTotalElements();
            if(MeetingUrlConstant.size < total ){
                int page = total/MeetingUrlConstant.size;
                if(total >(page*MeetingUrlConstant.size)){
                    page = page+1;
                }
                logger.info("已预约告警总记录数大于每页数量时处理，page为"+page);
                for(int i = 1;i <= page;i++){
                    urlStr = url+"/conf-portal" + MeetingUrlConstant.NOW_ALARM_URL;
                    urlStr = urlStr.replace("{0}", id).replace("{page}",i+"");
                    res = HttpClientUtils.doGet(urlStr, null, header);
                    logger.warn("getNowMeetingAlarm 接口返回：{}",res);
                    AlarmResBean alarmResBeanRes = gson.fromJson(res, AlarmResBean.class);
                    List<AlarmVo> contentListRes = alarmResBeanRes.getContent();
                    if(CollectionUtils.isNotEmpty(contentListRes)){
                        result.addAll(contentListRes);
                    }
                }
            }
            saveNowMeetingAlarm(result);
        } catch (Exception ex) {
            logger.error("保存历史会议-会议ID{}的告警信息失败！信息为={}", id, ex);
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

    private void saveNowMeetingAlarm(List<AlarmVo> result) {
        List<HwMeetingAlarm> alarms = new ArrayList<>();
        for (AlarmVo content : result) {
            HwMeetingAlarm alarm = new HwMeetingAlarm();
            alarm.setId(UUIDUtils.get32UUID());
            alarm.setMeetingId(content.getConfId());
            alarm.setName(content.getName());
            alarm.setAlarmNo(content.getAlarmNo());
            alarm.setAlarmTime(CronUtil.utcToLocal(content.getAlarmTime()));
            alarm.setAlarmType(content.getName());
            alarm.setAlarmStatus("current");
            alarms.add(alarm);
        }
        hwMeetingAlarmService.save(alarms);
    }


    @Override
    public int initMeetingRooms(){
        // 注册
        register();
        // 获取token
        String token = getSysToken();
        // 获取会议室接口
        if(StringUtils.isBlank(organizationId)){
            throw new RuntimeException("获取会议室列表时,组织结构id不能为空");
        }
        Map<String,String> header = new HashMap<>();
        header.put("token",token);
        header.put("Content-type","application/json;charset=UTF-8");
        Map<String,Object> param = new HashMap<>();
        param.put("organizationId",organizationId);
        param.put("searchtree",false);
        String roomUrl = url+"/sys-portal"+MeetingUrlConstant.MEETING_ROOMS_URL;
        String res = HttpClientUtils.doPost(roomUrl, param, header);
        logger.warn("initMeetingRooms 接口返回：{}",res);
        if(StringUtils.isNotBlank(res)){
            MeetingRoomsRes meetingRoomsRes = gson.fromJson(res,MeetingRoomsRes.class);
            int total = meetingRoomsRes.getTotalElements();
            return total;
        }
        return 0;
    }

    /**
     * 获取会议室时，首先注册   2023-07-24
     * curl -k -H "Content-Type:application/json" -X POST -d '{"sn":"0200040144508844","hd":"212612240G0000649"}'
     * "https://200.1.26.50/cmc-portal/noauth/deviceinfo"
     */
    private void register(){
;       Map<String,String> header = new HashMap<>();
        header.put("Content-Type","application/json");
        if(StringUtils.isEmpty(registerSn)){
            logger.error("执行注册时,sn的值不能为空");
            throw new RuntimeException("执行注册时,sn的值不能为空");
        }
        registerSn = registerSn.replace("$","");
        if(StringUtils.isEmpty(registerHd)){
            logger.error("执行注册时,hd的值不能为空");
            throw new RuntimeException("执行注册时,hd的值不能为空");
        }
        Map<String,Object> param = new HashMap<>();
        param.put("sn",registerSn);
        param.put("hd",registerHd);
        String roomUrl = url+"/cmc-portal"+MeetingUrlConstant.REGISTER;
        String res = HttpClientUtils.doPost(roomUrl, param, header);
        logger.info("注册接口反回内容：{}",res);
    }
    public String getSysToken(){
        String tokenRes = "";
        String tokenUrl = url +"/sys-portal" + MeetingUrlConstant.TOKEN_URL;
        logger.info("获取token时(sys-portal),用户名："+sysUserName+" ===密码："+sysPassword);
        Map<String, String> header = new HashMap<>();
        if(StringUtils.isEmpty(sysUserName)){
            throw new RuntimeException("获取token时,用户名不能为空");
        }
        if(StringUtils.isEmpty(sysPassword)){
            throw new RuntimeException("获取token时,用户密码不能为空");
        }
        String encode = Base64Utils.encodeBase64(sysUserName+":"+sysPassword);
        logger.warn("get sys token base64 encode={}", encode);
        header.put("Authorization", "Basic " + encode);
        try {
            String result = HttpClientUtils.doGet(tokenUrl, null, header);
            logger.warn("获取sys token = {}",result);
            Token token = gson.fromJson(result, Token.class);
            tokenRes = token.getUuid();
        } catch (Exception ex) {
            logger.error("get token error,msg={}", ex);
            throw new RuntimeException(ex);
        }
        return tokenRes;
    }
}

