package com.vrv.vap.apicasom.business.meeting.controller;

import com.vrv.vap.apicasom.business.meeting.service.SituationLargeScreenService;
import com.vrv.vap.apicasom.business.meeting.vo.*;
import com.vrv.vap.jpa.web.Result;
import com.vrv.vap.jpa.web.ResultCodeEnum;
import com.vrv.vap.jpa.web.ResultUtil;
import com.vrv.vap.syslog.common.annotation.SysRequestLog;
import com.vrv.vap.syslog.common.enums.ActionType;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * 态势应用大屏接口
 *
 * @author liurz
 * @data 2023-05-25
 */
@RestController
@RequestMapping(value = "/situationLargeScreen")
public class SituationLargeScreenController {
    private static Logger logger = LoggerFactory.getLogger(SituationLargeScreenController.class);

    @Autowired
    private SituationLargeScreenService situationLargeScreenService;
    /**
     * 公文及文件交换系统发件数量、收件数量{"type":"all"}
     * @return Result
     */
    @PostMapping(value = "/fileSendAndReceiveNum")
    @ApiOperation(value = "公文及文件交换系统发件数量、收件数量top10", notes = "")
    @SysRequestLog(description = "公文及文件交换系统发件数量、收件数量top10", actionType = ActionType.SELECT)
    public Result<FileSendAndReceiveNumVO> fileSendAndReceiveNum(@RequestBody SituationLargeSearchVO searchVO) {
        try {
            return ResultUtil.success(situationLargeScreenService.fileSendAndReceiveNum(searchVO));
        } catch (Exception e) {
            logger.error("公文及文件交换系统发件数量、收件数量top10异常,{}", e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(), "公文及文件交换系统发件数量、收件数量top10异常");
        }
    }
    /**
     * 发件和收件情况统计 {"type":"all"}
     * 1.本地区收件
     * 2.本地地区发件
     * 3.跨地区收件
     * 4.跨地区发件
     * 全部的话：根据实际最大时间和最小时间来判断
     *      大于2年按年统计
     *      大于1个月按月统计
     *      小于等于1个月按天统计
     * 全部的话，如果没有数据x轴为空
     * @return Result
     */
    @PostMapping(value = "/fileSendAndReceiveTrend")
    @ApiOperation(value = "发件和收件情况统计", notes = "")
    @SysRequestLog(description = "发件和收件情况统计", actionType = ActionType.SELECT)
    public Result<List<FileSendAndReceiveVO>> fileSendAndReceiveTrend(@RequestBody SituationLargeSearchVO searchVO) {
        try {
            return ResultUtil.successList(situationLargeScreenService.fileSendAndReceiveTrend(searchVO));
        } catch (Exception e) {
            logger.error("发件和收件情况统计异常,{}", e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(), "发件和收件情况统计异常");
        }
    }

    /**
     * 院机关各部门邮件收发数量  {"type":"all"}
     * @return Result
     */
    @PostMapping(value = "/emailSendAndReceiveNum")
    @ApiOperation(value = "院机关各部门邮件收发数量", notes = "")
    @SysRequestLog(description = "院机关各部门邮件收发数量", actionType = ActionType.SELECT)
    public Result<EmailSendAndReceiveNumVO> emailSendAndReceiveNum(@RequestBody SituationLargeSearchVO searchVO) {
        try {
            return ResultUtil.success(situationLargeScreenService.emailSendAndReceiveNum(searchVO));
        } catch (Exception e) {
            logger.error("院机关各部门邮件收发数量异常,{}", e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(), "院机关各部门邮件收发数量异常");
        }
    }

    /**
     * 收发件数量  {"type":"all","tabName":"1"}
     * tabName "1":各分院(地区)  "2":院机关各部门
     * 1.本地区收件
     * 2.本地地区发件
     * 3.跨地区收件
     * 4.跨地区发件
     * @return Result
     */
    @PostMapping(value = "/fileSendAndReceiveTab")
    @ApiOperation(value = "收发件数量", notes = "")
    @SysRequestLog(description = "收发件数量", actionType = ActionType.SELECT)
    public Result<List<FileSendAndReceiveVO>> fileSendAndReceiveTab(@RequestBody SituationLargeSearchVO searchVO) {
        try {
            String tabName = searchVO.getTabName();
            // 为空默认为：各分院
            if(StringUtils.isEmpty(tabName)){
                tabName ="1";
            }
            return ResultUtil.successList(situationLargeScreenService.fileSendAndReceiveTab(searchVO,tabName));
        } catch (Exception e) {
            logger.error("发件和收件情况统计异常,{}", e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(), "发件和收件情况统计异常");
        }
    }

    /**
         * 打印和刻录数量 {"type":"month"}
     * @return Result
     */
    @PostMapping(value = "/printingAndBurningNum")
    @ApiOperation(value = "打印和刻录数量", notes = "")
    @SysRequestLog(description = "打印和刻录数量", actionType = ActionType.SELECT)
    public Result<List<PrintingAndBurningNumVO>> printingAndBurningNum(@RequestBody SituationLargeSearchVO searchVO) {
        try {
            return situationLargeScreenService.printingAndBurningNum(searchVO);
        } catch (Exception e) {
            logger.error("打印和刻录数量异常,{}", e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(), "打印和刻录数量异常");
        }
    }

    /**
     * 公文交换箱系统情况  {"type":"month"}
     * type： month(近一个月)、halfyear(半年)、year(一年)
     * @return Result
     */
    @PostMapping(value = "/exchangeBox")
    @ApiOperation(value = "公文交换箱系统情况", notes = "")
    @SysRequestLog(description = "公文交换箱系统情况", actionType = ActionType.SELECT)
    public Result<ExchangeBoxVO> exchangeBox(@RequestBody SituationLargeSearchVO searchVO) {
        try {
            return ResultUtil.success(situationLargeScreenService.exchangeBox(searchVO));
        } catch (Exception e) {
            logger.error("公文交换箱系统情况异常,{}", e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(), "公文交换箱系统情况异常");
        }
    }
    /**
     * 本地区/跨地区文件交换占比
     *{"beginTime":"2023-03-01","endTime":"2023-03-02"}
     * @return Result
     */
    @PostMapping(value = "/fileExchangePer")
    @ApiOperation(value = "本地区/跨地区文件交换占比", notes = "")
    @SysRequestLog(description = "本地区/跨地区文件交换占比", actionType = ActionType.SELECT)
    public Result<Map<String,Object>> fileExchangePer(@RequestBody SituationLargeSearchVO searchVO) {
        try {
            return ResultUtil.success(situationLargeScreenService.fileExchangePer(searchVO));
        } catch (Exception e) {
            logger.error("本地区/跨地区文件交换占比异常,{}", e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(), "本地区/跨地区文件交换占比异常");
        }
    }

    /**
     * 地图
     *
     * @return Result
     */
    @PostMapping(value = "/branchMap")
    @ApiOperation(value = "地图", notes = "")
    @SysRequestLog(description = "地图", actionType = ActionType.SELECT)
    public Result<List<String>> branchMap(@RequestBody SituationLargeSearchVO searchVO) {
        try {
            return ResultUtil.successList(situationLargeScreenService.branchMap(searchVO));
        } catch (Exception e) {
            logger.error("地图异常,{}", e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(), "地图异常");
        }
    }
    /**
     * 地图详情
     * 1.根据city和时间范围查询
     * @return Result
     */
    @PostMapping(value = "/cityMapDetail")
    @ApiOperation(value = "地图详情", notes = "")
    @SysRequestLog(description = "地图详情", actionType = ActionType.SELECT)
    public Result<List<MapDetailVO>> cityMapDetail(@RequestBody SituationLargeSearchVO searchVO) {
        try {
            return ResultUtil.successList(situationLargeScreenService.cityMapDetail(searchVO));
        } catch (Exception e) {
            logger.error("地图详情异常,{}", e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(), "地图详情异常");
        }
    }
}
