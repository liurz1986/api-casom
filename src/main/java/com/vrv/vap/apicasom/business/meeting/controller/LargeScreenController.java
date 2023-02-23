package com.vrv.vap.apicasom.business.meeting.controller;

import com.vrv.vap.apicasom.business.meeting.service.LargeScreenService;
import com.vrv.vap.apicasom.business.meeting.util.MettingCommonUtil;
import com.vrv.vap.apicasom.business.meeting.vo.CommonSearchVO;
import com.vrv.vap.apicasom.business.meeting.vo.LargeMapDetailVO;
import com.vrv.vap.apicasom.business.meeting.vo.LargeMapVO;
import com.vrv.vap.apicasom.business.meeting.vo.LargeScreenBaseMessageVO;
import com.vrv.vap.common.utils.StringUtils;
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
 * 大屏接口
 *
 * @author var
 * @data 2023-02-22
 */
@RestController
@RequestMapping(value = "/largeScreen")
public class LargeScreenController {
    private static Logger logger = LoggerFactory.getLogger(LargeScreenController.class);

    @Autowired
    private LargeScreenService largeScreenService;
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
     * type:quarter(季)，halfyear(半年)、year(一年)
     * @return Result
     */
    @GetMapping(value = "/queryMapMesage/{type}")
    @ApiOperation(value = "地图查询", notes = "")
    @SysRequestLog(description = "地图查询", actionType = ActionType.SELECT)
    public Result<List<LargeMapVO>> queryMapMesage(@PathVariable("type") String type) {
        try {
            if(StringUtils.isEmpty(type)){
                return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(), "type的值不能为空");
            }
            if(!MettingCommonUtil.isExistLargeTimeType(type)){
                return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"传参type的值有误");
            }
            return ResultUtil.successList(largeScreenService.queryMapMesage(type));
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
    @PostMapping(value = "/queryCityDetail")
    @ApiOperation(value = "地图城市详情", notes = "")
    @SysRequestLog(description = "地图城市详情", actionType = ActionType.SELECT)
    public Result<LargeMapDetailVO> queryCityDetail(@RequestBody CommonSearchVO commonSearchVO) {
        try {
            if(StringUtils.isEmpty(commonSearchVO.getType())){
                return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(), "type的值不能为空");
            }
            if(StringUtils.isEmpty(commonSearchVO.getCityName())){
                return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(), "城市的值不能为空");
            }
            return ResultUtil.success(largeScreenService.queryCityDetail(commonSearchVO));
        } catch (Exception e) {
            logger.error("地图查询异常,{}", e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(), "地图查询异常");
        }
    }
}
