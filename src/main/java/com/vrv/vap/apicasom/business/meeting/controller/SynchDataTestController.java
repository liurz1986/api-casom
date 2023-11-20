package com.vrv.vap.apicasom.business.meeting.controller;
import com.vrv.vap.apicasom.business.task.constant.MeetingUrlConstant;
import com.vrv.vap.apicasom.business.task.service.MeetingHttpService;
import com.vrv.vap.apicasom.business.task.service.impl.HistoryHwMeetingDataServiceImpl;
import com.vrv.vap.apicasom.business.task.service.impl.ReservationHwMeetingDataServiceImpl;
import com.vrv.vap.apicasom.frameworks.util.HttpClientUtils;
import com.vrv.vap.apicasom.frameworks.util.MeetingUtil;
import com.vrv.vap.apicasom.frameworks.util.RedisUtils;
import com.vrv.vap.jpa.web.Result;
import com.vrv.vap.jpa.web.ResultUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试用接口，没有其他作用
 */
@RestController
@RequestMapping("test")
public class SynchDataTestController {
    private static Logger logger = LoggerFactory.getLogger(SynchDataTestController.class);
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private ReservationHwMeetingDataServiceImpl reservationHwMeetingDataService;
    @Autowired
    private HistoryHwMeetingDataServiceImpl historyHwMeetingDataService;
    @Autowired
    private MeetingHttpService meetingHttpService;

    @Value("${hw.meeting.url}")
    private String url;

    /**
     * 清楚redis中数据--测试用的
     * meetingTime:预约会议，同步时记录的上次同步时间
     * hisMeetingTime：历史会议，同步时记录的上次同步时间
     * @return
     */
    @GetMapping("clearRedis")
    public  Result<String> clearRedis(@RequestParam("key") String key){
        redisUtils.del(key);
        return ResultUtil.success("success");
    }
    /**
     * 手动同步--测试用
     * type：1 预约会议 2 历史会议
     * startTime：开始时间
     * endTime：历史时间
     * @param type
     * @param startTime
     * @param endTime
     */
    @GetMapping("syncData")
    public Result<String> syncData(@RequestParam("type") String type,@RequestParam("startTime") String startTime,@RequestParam("endTime") String endTime){
        if(StringUtils.isEmpty(type)||StringUtils.isEmpty(startTime)||StringUtils.isEmpty(endTime)){
            logger.info("手动同步,type、startTime、endTime其中一个为空不执行同步");
            return ResultUtil.success("type、startTime、endTime其中一个为空不执行同步");
        }
        logger.info("手动执行同步");
        // 预约会议
        if("1".equals(type)){
            reservationHwMeetingDataService.syncData(startTime,endTime);
        }
        if("2".equals(type)){
            historyHwMeetingDataService.syncData(startTime,endTime);
        }
        return ResultUtil.success("success");
    }

    /**
     * 查询预约会议列表
     * @param startTime
     * @param endTime
     */
    @GetMapping("nowMettingList")
    public Result<String> nowMettingList(@RequestParam("startTime") String startTime, @RequestParam("endTime") String endTime) {
        String token = meetingHttpService.getToken(0);
        MeetingUtil.token= token;
        Map<String, String> header = getHeader();
        Map<String, Object> param = new HashMap<>();
        param.put("startTime", startTime+" UTC");
        param.put("endTime", endTime+" UTC");
        param.put("active",true);
        String nowMeetingUrl = url+"/conf-portal" + MeetingUrlConstant.NOW_LIST_URL + "";
        nowMeetingUrl = nowMeetingUrl.replace("{page}","0");
        String res = HttpClientUtils.doPost(nowMeetingUrl, param, header);
        return ResultUtil.success(res);
    }
    /**
     * 查询预约会议详情
     * id:会议id
     */
    @GetMapping("nowMettingDeatil")
    public Result<String> nowMettingDeatil(@RequestParam("id") String id) {
        String token = meetingHttpService.getToken(0);
        MeetingUtil.token= token;
        Map<String, String> header = getHeader();
        String nowMeetingInfoUrl = url+"/conf-portal" + MeetingUrlConstant.NOW_INFO_URL;
        nowMeetingInfoUrl = nowMeetingInfoUrl.replace("{0}", id);
        String res = HttpClientUtils.doGet(nowMeetingInfoUrl, null, header);
        return ResultUtil.success(res);
    }
    /**
     * 查询预约会场
     * id:会议id
     */
    @GetMapping("nowParticipants")
    public Result<String> nowParticipants(@RequestParam("id") String id){
        String token = meetingHttpService.getToken(0);
        MeetingUtil.token= token;
        Map<String, String> header = getHeader();
        String nowMeetingParticipant = url+"/conf-portal" + MeetingUrlConstant.NOW_PARTICIPANT_URL;
        nowMeetingParticipant = nowMeetingParticipant.replace("{0}", id).replace("{page}","0");
        Map<String,Object> param = new HashMap<>();
        String res = HttpClientUtils.doPost(nowMeetingParticipant, param, header);
        return ResultUtil.success(res);
    }
    /**
     * 查询预约告警
     * id:会议id
     */
    @GetMapping("nowAlarm")
    public Result<String> nowAlarm(@RequestParam("id") String id){
        String token = meetingHttpService.getToken(0);
        MeetingUtil.token= token;
        Map<String, String> header = getHeader();
        String urlStr = url+"/conf-portal" + MeetingUrlConstant.NOW_ALARM_URL;
        urlStr = urlStr.replace("{0}", id).replace("{page}","0");
        String res = HttpClientUtils.doGet(urlStr, null, header);
        return ResultUtil.success(res);
    }

    /**
     * 查询历史会议列表
     * @param startTime
     * @param endTime
     */
    @GetMapping("hisMettingList")
    public Result<String> hisMettingList(@RequestParam("startTime") String startTime, @RequestParam("endTime") String endTime) {
        String token = meetingHttpService.getToken(0);
        MeetingUtil.token= token;
        Map<String, String> header = getHeader();
        Map<String, Object> param = new HashMap<>();
        param.put("startTime", startTime+" UTC");
        param.put("endTime", endTime+" UTC");
        String urlStr = url+"/conf-portal" + MeetingUrlConstant.HISTORY_LIST_URL;
        urlStr=  urlStr.replace("{page}","0");
        String res = HttpClientUtils.doPost(urlStr, param, header);
        return ResultUtil.success(res);
    }

    /**
     * 查询历史会议详情
     * id:会议id
     */
    @GetMapping("hisMettingDeatil")
    public Result<String> hisMettingDeatil(@RequestParam("id") String id) {
        String token = meetingHttpService.getToken(0);
        MeetingUtil.token= token;
        Map<String, String> header = getHeader();
        String infoUrl = url+"/conf-portal" + MeetingUrlConstant.HISTORY_INFO_URL;
        infoUrl = infoUrl.replace("{0}", id);
        String infoStr = HttpClientUtils.doGet(infoUrl, null, header);
        return ResultUtil.success(infoStr);
    }

    /**
     * 查询历史会议告警
     * id:会议id
     */
    @GetMapping("hisAlarm")
    public Result<String> hisAlarm(@RequestParam("id") String id){
        String token = meetingHttpService.getToken(0);
        MeetingUtil.token= token;
        Map<String, String> header = getHeader();
        String urlStr = url+"/conf-portal" + MeetingUrlConstant.HISTORY_ALARM_URL;
        urlStr = urlStr.replace("{0}", id).replace("{page}", "0");
        Map<String,Object> param = new HashMap<>();
        String res = HttpClientUtils.doPost(urlStr, param, header);
        return ResultUtil.success(res);
    }

    /**
     * 会议室数量
     */
    @GetMapping("mettingNum")
    public Result<Integer> mettingNum(){
        int count= meetingHttpService.initMeetingRooms();
        return ResultUtil.success(count);
    }

    /**
     * 获取请求头
     *
     * @return
     */
    public Map<String, String> getHeader() {
        Map<String, String> header = new HashMap<>();
        header.put("token", MeetingUtil.token);
        header.put("Content-type","application/json;charset=UTF-8");
        return header;
    }
}
