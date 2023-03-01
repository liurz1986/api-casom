package com.vrv.vap.apicasom.business.meeting.controller;

import com.vrv.vap.apicasom.business.meeting.service.LargeScreenService;
import com.vrv.vap.apicasom.business.meeting.util.MettingCommonUtil;
import com.vrv.vap.apicasom.business.meeting.vo.*;
import com.vrv.vap.apicasom.business.task.bean.HwMeetingAttendee;
import com.vrv.vap.apicasom.business.task.bean.HwMeetingInfo;
import com.vrv.vap.apicasom.business.task.bean.HwMeetingParticipant;
import com.vrv.vap.apicasom.business.task.service.HwMeetingAlarmService;
import com.vrv.vap.apicasom.business.task.service.HwMeetingAttendeeService;
import com.vrv.vap.apicasom.business.task.service.HwMeetingInfoService;
import com.vrv.vap.apicasom.business.task.service.HwMeetingParticipantService;
import com.vrv.vap.common.utils.StringUtils;
import com.vrv.vap.jpa.common.DateUtil;
import com.vrv.vap.jpa.common.UUIDUtils;
import com.vrv.vap.jpa.web.Result;
import com.vrv.vap.jpa.web.ResultCodeEnum;
import com.vrv.vap.jpa.web.ResultUtil;
import com.vrv.vap.syslog.common.annotation.SysRequestLog;
import com.vrv.vap.syslog.common.enums.ActionType;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * 大屏接口
 *
 * @author liurz
 * @data 2023-02-22
 */
@RestController
@RequestMapping(value = "/largeScreen")
public class LargeScreenController {
    private static Logger logger = LoggerFactory.getLogger(LargeScreenController.class);

    @Autowired
    private LargeScreenService largeScreenService;
    @Autowired
    private HwMeetingInfoService hwMeetingInfoService;
    @Autowired
    private HwMeetingParticipantService hwMeetingParticipantService;
    @Autowired
    private HwMeetingAttendeeService hwMeetingAttendeeService;
    /**
     * 基本信息：会议视屏节点总数、当前节点在线总数、举办会议次数、参会总人数、会议总时长
     * type:quarter(季)，halfyear(半年)、year(一年)
     * @return Result
     */
    @GetMapping(value = "/queryBaseMessage/{type}")
    @ApiOperation(value = "会议大屏基本信息展示", notes = "")
    @SysRequestLog(description = "会议大屏基本信息展示", actionType = ActionType.SELECT)
    public Result<LargeScreenBaseMessageVO> queryBaseMessage(@PathVariable("type") String type) {
        try {
            if(StringUtils.isEmpty(type)){
                return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(), "type的值不能为空");
            }
            if(!MettingCommonUtil.isExistLargeTimeType(type)){
                return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"传参type的值有误");
            }
            return ResultUtil.success(largeScreenService.queryBaseMessage(type));
        } catch (Exception e) {
            logger.error("获得资产总数异常,{}", e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(), "会议大屏基本信息展示异常");
        }
    }

    /**
     * 地图查询：
     *
     * @return Result
     */
    @GetMapping(value = "/queryMapMesage")
    @ApiOperation(value = "地图查询", notes = "")
    @SysRequestLog(description = "地图查询", actionType = ActionType.SELECT)
    public Result<List<LargeMapVO>> queryMapMesage() {
        try {
            return ResultUtil.successList(largeScreenService.queryMapMesage());
        } catch (Exception e) {
            logger.error("地图查询异常,{}", e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(), "地图查询异常");
        }
    }
    /**
     * 地图城市详情
     * type:quarter(季)，halfyear(半年)、year(一年)
     * @return Result
     */
    @GetMapping(value = "/queryCityDetail")
    @ApiOperation(value = "地图城市详情", notes = "")
    @SysRequestLog(description = "地图城市详情", actionType = ActionType.SELECT)
    public Result<LargeMapDetailVO> queryCityDetail(HttpServletRequest request) {
        try {
            String cityName = request.getParameter("cityName");
            if(StringUtils.isEmpty(cityName)){
                return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(), "城市的值不能为空");
            }
            CommonSearchVO commonSearchVO = new CommonSearchVO();
            commonSearchVO.setCityName(cityName);
            return ResultUtil.success(largeScreenService.queryCityDetail(commonSearchVO));
        } catch (Exception e) {
            logger.error("地图查询异常,{}", e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(), "地图查询异常");
        }
    }

    /**
     * 各地区系统使用统计
     * type:quarter(季)，halfyear(半年)、year(一年)
     * @return Result
     */
    @GetMapping(value = "/queryBranchStatistics/{type}")
    @ApiOperation(value = "各地区系统使用统计", notes = "")
    @SysRequestLog(description = "各地区系统使用统计", actionType = ActionType.SELECT)
    public Result<List<LargeBranchStatisticsVO>> queryBranchStatistics(@PathVariable("type") String type) {
        try {
            if(StringUtils.isEmpty(type)){
                return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(), "type的值不能为空");
            }
            if(!MettingCommonUtil.isExistLargeTimeType(type)){
                return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"传参type的值有误");
            }
            return ResultUtil.successList(largeScreenService.queryBranchStatistics(type));
        } catch (Exception e) {
            logger.error("各地区系统使用统计异常,{}", e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(), "各地区系统使用统计异常");
        }
    }

    /**
     * 点对点会议次数、各地区使用占比
     *1. 各地区使用占比 展示前5的数据，还剩的用其他统计
     * type:quarter(季)，halfyear(半年)、year(一年)
     * @return Result
     */
    @GetMapping(value = "/queryBranchScaleStatistics/{type}")
    @ApiOperation(value = "点对点会议次数、各地区使用占比统计", notes = "")
    @SysRequestLog(description = "点对点会议次数、各地区使用占比统计", actionType = ActionType.SELECT)
    public Result<LargeBranchUseScaleStatisticsVO> queryBranchScaleStatistics(@PathVariable("type") String type) {
        try {
            if(StringUtils.isEmpty(type)){
                return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(), "type的值不能为空");
            }
            if(!MettingCommonUtil.isExistLargeTimeType(type)){
                return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"传参type的值有误");
            }
            return ResultUtil.success(largeScreenService.queryBranchScaleStatistics(type));
        } catch (Exception e) {
            logger.error("点对点会议次数、各地区使用占比统计异常,{}", e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(), "点对点会议次数、各地区使用占比统计异常");
        }
    }
    /**
     * 多点会议次数
     *
     * type:quarter(季)，halfyear(半年)、year(一年)
     * @return Result
     */
    @GetMapping(value = "/getManyPoint/{type}")
    @ApiOperation(value = "多点会议次数", notes = "")
    @SysRequestLog(description = "多点会议次数", actionType = ActionType.SELECT)
    public Result<Integer> getManyPoint(@PathVariable("type") String type) {
        try {
            if(StringUtils.isEmpty(type)){
                return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(), "type的值不能为空");
            }
            if(!MettingCommonUtil.isExistLargeTimeType(type)){
                return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"传参type的值有误");
            }
            return ResultUtil.success(largeScreenService.getManyPoint(type));
        } catch (Exception e) {
            logger.error("多点会议次数异常,{}", e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(), "多点会议次数异常");
        }
    }
    /**
     * 异常及故障情况分析
     *  1. 异常及故障情况分析 展示前5的数据，还剩的用其他统计
     * type:quarter(季)，halfyear(半年)、year(一年)
     * @return Result
     */
    @GetMapping(value = "/queryBranchAbnormalStatistics/{type}")
    @ApiOperation(value = "异常及故障情况分析", notes = "")
    @SysRequestLog(description = "异常及故障情况分析", actionType = ActionType.SELECT)
    public Result<List<LargeDeatailVO>> queryBranchAbnormalStatistics(@PathVariable("type") String type) {
        try {
            if(StringUtils.isEmpty(type)){
                return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(), "type的值不能为空");
            }
            if(!MettingCommonUtil.isExistLargeTimeType(type)){
                return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"传参type的值有误");
            }
            return ResultUtil.successList(largeScreenService.queryBranchAbnormalStatistics(type));
        } catch (Exception e) {
            logger.error("异常及故障情况分析异常,{}", e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(), "异常及故障情况分析异常");
        }
    }
    /**
     * 开会次数
     *
     * type:quarter(季)，halfyear(半年)、year(一年)
     * @return Result
     */
    @GetMapping(value = "/queryNodeMeetingCountStatistics/{type}")
    @ApiOperation(value = "开会次数", notes = "")
    @SysRequestLog(description = "开会次数", actionType = ActionType.SELECT)
    public Result<List<LargeDeatailVO>> queryNodeMeetingCountStatistics(@PathVariable("type") String type) {
        try {
            if(StringUtils.isEmpty(type)){
                return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(), "type的值不能为空");
            }
            if(!MettingCommonUtil.isExistLargeTimeType(type)){
                return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"传参type的值有误");
            }
            return ResultUtil.success(largeScreenService.queryNodeMeetingCountStatistics(type));
        } catch (Exception e) {
            logger.error("开会次数异常,{}", e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(), "开会次数异常");
        }
    }

    /**
     * 对外提供服务
     *
     * type:quarter(季)，halfyear(半年)、year(一年)
     * @return Result
     */
    @GetMapping(value = "/queryOutServiceStatistics/{type}")
    @ApiOperation(value = "对外提供服务", notes = "")
    @SysRequestLog(description = "对外提供服务", actionType = ActionType.SELECT)
    public Result<List<LargeDeatailVO>> queryNodeMeetingCountOutStatistics(@PathVariable("type") String type) {
        try {
            if(StringUtils.isEmpty(type)){
                return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(), "type的值不能为空");
            }
            if(!MettingCommonUtil.isExistLargeTimeType(type)){
                return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"传参type的值有误");
            }
            return ResultUtil.success(largeScreenService.queryOutServiceStatistics(type));
        } catch (Exception e) {
            logger.error("对外提供服务异常,{}", e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(), "对外提供服务异常");
        }
    }

    /**
     * 模拟数据用的接口
     * @throws ParseException
     * @throws InterruptedException
     */
    @GetMapping(value = "/dataSave")
    public void testSave() throws ParseException, InterruptedException {
        List<HwMeetingInfo> mettings = new ArrayList<>();
        List<HwMeetingParticipant> hwMeetingParticipants = new ArrayList<>();
        List<HwMeetingAttendee> hwMeetingAttendees = new ArrayList<>();
        HwMeetingInfo hwMeetingInfo = null;
        for(int i= 0;i < 400;i++){
            hwMeetingInfo = getMeettingInfo();
            List<HwMeetingParticipant> participants = new ArrayList<>();
            List<HwMeetingAttendee> attendees = new ArrayList<>();
            getParticipants(participants,attendees,hwMeetingInfo);
            hwMeetingParticipants.addAll(participants);
            hwMeetingAttendees.addAll(attendees);
            mettings.add(hwMeetingInfo);
        }
        hwMeetingInfoService.save(mettings);
        hwMeetingParticipantService.save(hwMeetingParticipants);
        hwMeetingAttendeeService.save(hwMeetingAttendees);
    }

    private List<HwMeetingParticipant> getParticipants(List<HwMeetingParticipant> hwMeetingParticipants,List<HwMeetingAttendee> hwMeetingAttendees,HwMeetingInfo hwMeetingInfo) throws ParseException {
        //,计算机网络信息中心,近代物理研究所
        HwMeetingParticipant cipant =getHwMeetingParticipant(hwMeetingInfo,"北京基因组研究所","北京","北京");
        hwMeetingParticipants.add(cipant);
        HwMeetingAttendee hwMeetingAttendee = getHwMeetingAttendee(hwMeetingInfo,"北京基因组研究所","北京","北京");
        hwMeetingAttendees.add(hwMeetingAttendee);

        cipant =getHwMeetingParticipant(hwMeetingInfo,"南海海洋研究所","广州","广州分院");
        hwMeetingParticipants.add(cipant);
        hwMeetingAttendee = getHwMeetingAttendee(hwMeetingInfo,"南海海洋研究所","广州","广州分院");
        hwMeetingAttendees.add(hwMeetingAttendee);

        return hwMeetingParticipants;
    }

    private HwMeetingAttendee getHwMeetingAttendee(HwMeetingInfo hwMeetingInfo,String name,String city,String branch) {
        HwMeetingAttendee hwMeetingAttendee =new HwMeetingAttendee();
        hwMeetingAttendee.setMeetingId(hwMeetingInfo.getMeetingId());
        hwMeetingAttendee.setBranch(branch);
        hwMeetingAttendee.setCity(city);
        hwMeetingAttendee.setDuration(50);
        hwMeetingAttendee.setUserCount(1);
        hwMeetingAttendee.setParticipantName(name);
        hwMeetingAttendee.setId(UUIDUtils.get32UUID());
        return hwMeetingAttendee;
    }

    private HwMeetingParticipant getHwMeetingParticipant(HwMeetingInfo hwMeetingInfo,String name,String city,String branch) {
        HwMeetingParticipant cipant = new HwMeetingParticipant();
        cipant.setDuration(50);
        cipant.setMeetingId(hwMeetingInfo.getMeetingId());
        cipant.setStage("OFFLINE");
        cipant.setScheduleStartTime(hwMeetingInfo.getScheduleStartTime());
        cipant.setScheduleEndTime(hwMeetingInfo.getScheduleEndTime());
        cipant.setCity(city);
        cipant.setBranch(branch);
        cipant.setTerminalType("型号XXX");
        cipant.setName(name);
        cipant.setOrganizationName(name);
        cipant.setId(UUIDUtils.get32UUID());
        return cipant;
    }

    private HwMeetingInfo getMeettingInfo() throws ParseException {
        HwMeetingInfo hwMeetingInfo = new HwMeetingInfo();
        hwMeetingInfo.setMeetingId(UUIDUtils.get32UUID());
        Date current = new Date();
        hwMeetingInfo.setScheduleStartTime(current);
        long endTime = current.getTime()+50*60*1000;
        Date date = new Date(endTime);
        hwMeetingInfo.setScheduleEndTime(date);
        hwMeetingInfo.setDuration(50);
        hwMeetingInfo.setAttendeeCount(6);
        hwMeetingInfo.setParticipantCount(6);
        hwMeetingInfo.setParticipantUnity("北京基因组研究所,南海海洋研究所");
        hwMeetingInfo.setOrganizationName("北京基因组研究所,南海海洋研究所");
        hwMeetingInfo.setStage("OFFLINE");
        return hwMeetingInfo;
    }
}
