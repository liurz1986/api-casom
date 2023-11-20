package com.vrv.vap.apicasom.business.meeting.controller;

import com.vrv.vap.apicasom.business.meeting.service.MeetingService;
import com.vrv.vap.apicasom.business.task.service.impl.HistoryHwMeetingDataServiceImpl;
import com.vrv.vap.apicasom.business.task.service.impl.ReservationHwMeetingDataServiceImpl;
import com.vrv.vap.apicasom.frameworks.util.RedisUtils;
import com.vrv.vap.jpa.web.Result;
import com.vrv.vap.jpa.web.ResultUtil;
import com.vrv.vap.syslog.common.annotation.SysRequestLog;
import com.vrv.vap.syslog.common.enums.ActionType;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 会议字典数据展示
 * @author liurz
 * @Date 202302
 */
@RestController
@RequestMapping("mettingConfig")
public class MeettingBaseConfigController {
    private static Logger logger = LoggerFactory.getLogger(MeettingBaseConfigController.class);
    @Autowired
    private MeetingService meetingService;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private ReservationHwMeetingDataServiceImpl reservationHwMeetingDataService;
    @Autowired
    private HistoryHwMeetingDataServiceImpl historyHwMeetingDataService;
    /**
     * 节点接入列表中节点名称列表
     * @return
     */
    @GetMapping("getNodeNames")
    @SysRequestLog(description="查询所有节点名称", actionType = ActionType.SELECT,manually = false)
    @ApiOperation(value="查询所有节点名称",notes="")
    public Result<List<String>> getNodeNames(){
        return ResultUtil.successList(meetingService.getNodeNames());
    }

    /**
     * 所属分院/地区数据
     * @return
     */
    @GetMapping("getRegions")
    @SysRequestLog(description="查询所有所属分院/地区数据", actionType = ActionType.SELECT,manually = false)
    @ApiOperation(value="查询所有所属分院/地区数据",notes="")
    public Result<List<String>> getRegions(){
        return ResultUtil.successList(meetingService.getRegions());
    }

    /**
     * 清楚redis中数据--测试用的
     * meetingTime:预约会议，同步时记录的上次同步时间
     * hisMeetingTime：历史会议，同步时记录的上次同步时间
     * @return
     */
    @GetMapping("clearRedis")
    public void clearRedis(@RequestParam("key") String key){
        redisUtils.del(key);
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
    public void syncData(@RequestParam("type") String type,@RequestParam("startTime") String startTime,@RequestParam("endTime") String endTime){
        if(StringUtils.isEmpty(type)||StringUtils.isEmpty(startTime)||StringUtils.isEmpty(endTime)){
            logger.info("手动同步,type、startTime、endTime其中一个为空不执行同步");
            return;
        }
        logger.info("手动执行同步");
        // 预约会议
        if("1".equals(type)){
            reservationHwMeetingDataService.syncData(startTime,endTime);
        }
        if("2".equals(type)){
            historyHwMeetingDataService.syncData(startTime,endTime);
        }
    }

}
