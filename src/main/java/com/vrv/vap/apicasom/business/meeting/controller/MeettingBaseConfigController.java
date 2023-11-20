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

    @Autowired
    private MeetingService meetingService;

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



}
