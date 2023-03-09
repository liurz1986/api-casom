package com.vrv.vap.apicasom.business.meeting.controller;

/**
 * 综合大屏
 * @author liurz
 *
 */

import com.vrv.vap.apicasom.business.meeting.service.IntegratedLargeScreenService;
import com.vrv.vap.apicasom.business.meeting.vo.*;
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
import java.util.List;

/**
 * 综合大屏接口
 *
 * @author liurz
 * @data 2023-03-7
 */
@RestController
@RequestMapping(value = "/integratedlargeScreen")
public class IntegratedLargeScreenController {
    private static Logger logger = LoggerFactory.getLogger(IntegratedLargeScreenController.class);


    @Autowired
    private IntegratedLargeScreenService integratedLargeScreenService;

    /**
     * 基本信息展示(视频会议系统节点数量、当前在线节点数、当前开会节点数、参会总人数、会议总时长、开会次数、发送文件、接收文件)
     * @return Result
     */
    @PostMapping(value = "/queryBasemessage")
    @ApiOperation(value = "综合大屏基本信息展示", notes = "")
    @SysRequestLog(description = "综合大屏基本信息展示", actionType = ActionType.SELECT)
    public Result<IntegratedlsBaseVO> queryBaseMessage(@RequestBody IntegratedLargeSearchVO searchVO) {
        try {
            return ResultUtil.success(integratedLargeScreenService.queryBaseMessage(searchVO));
        } catch (Exception e) {
            logger.error("综合大屏基本信息展示异常,{}", e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(), "综合大屏基本信息展示异常");
        }
    }

    /**
     * 概览：接入节点总数
     *
     * @return Result
     */
    @GetMapping(value = "/queryNodes")
    @ApiOperation(value = "接入节点总数", notes = "")
    @SysRequestLog(description = "接入节点总数", actionType = ActionType.SELECT)
    public Result<Long> queryNodes() {
        try {
            return ResultUtil.success(integratedLargeScreenService.queryNodes());
        } catch (Exception e) {
            logger.error("接入节点总数异常,{}", e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(), "接入节点总数异常");
        }
    }

    /**
     * 各地区接入节点分布
     * 接入节点表状态为历史，按分院分组统计数量
     * @return Result
     */
    @GetMapping(value = "/queryBranchNodeStatistics")
    @ApiOperation(value = "各地区接入节点分布", notes = "")
    @SysRequestLog(description = "各地区接入节点分布", actionType = ActionType.SELECT)
    public Result<List<KeyValueQueryVO>> queryBranchNodeStatistics() {
        try {
            return ResultUtil.successList(integratedLargeScreenService.queryBranchNodeStatistics());
        } catch (Exception e) {
            logger.error("各地区接入节点分布异常,{}", e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(), "各地区接入节点分布异常");
        }
    }

    /**
     * 应用使用态势
     *
     * 会议记录表状态为历史，时间段分组统计次数
     * 开始时间-结束时间相差
     *  小于等于24H，按小时统计
     *  大于24H小于等于1个月 ，按天统计
     *  大于1个月，按月统计
     * @return Result
     */
    @PostMapping(value = "/queryMettingsStatistics")
    @ApiOperation(value = "各地区接入节点分布", notes = "")
    @SysRequestLog(description = "各地区接入节点分布", actionType = ActionType.SELECT)
    public Result<List<KeyValueQueryVO>> queryMettingsStatistics(@RequestBody IntegratedLargeSearchVO searchVO) {
        try {
            if(null == searchVO.getBeginTime()){
                return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(), "开始日期不能为空");
            }
            if(null == searchVO.getEndTime()){
                return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(), "结束日期不能为空");
            }
            return ResultUtil.successList(integratedLargeScreenService.queryMettingsStatistics(searchVO));
        } catch (Exception e) {
            logger.error("各地区接入节点分布异常,{}", e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(), "各地区接入节点分布异常");
        }
    }

}
