package com.vrv.vap.apicasom.business.meeting.controller;

import com.vrv.vap.apicasom.business.meeting.service.VideoMettingService;
import com.vrv.vap.apicasom.business.meeting.util.MettingCommonUtil;
import com.vrv.vap.apicasom.business.meeting.vo.DistributionStatisticsVO;
import com.vrv.vap.apicasom.business.meeting.vo.StatisticSearchVO;
import com.vrv.vap.apicasom.business.meeting.vo.VideoMettingSearchVO;
import com.vrv.vap.apicasom.business.meeting.vo.VideoMettingVO;
import com.vrv.vap.jpa.web.Result;
import com.vrv.vap.jpa.web.ResultCodeEnum;
import com.vrv.vap.jpa.web.ResultUtil;
import com.vrv.vap.jpa.web.page.PageRes;
import com.vrv.vap.syslog.common.annotation.SysRequestLog;
import com.vrv.vap.syslog.common.enums.ActionType;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 视屏会议
 */
@RestController
@RequestMapping("videoMetting")
public class VideoMettingController {

    private static Logger logger = LoggerFactory.getLogger(VideoMettingController.class);
    @Autowired
    private VideoMettingService videoMettingService;
    /**
     * 会议时长分布统计
     *  {"type":"halfyear"}
     */
    @PostMapping("durationStatistics")
    @SysRequestLog(description="会议时长分布统计", actionType = ActionType.SELECT,manually = false)
    @ApiOperation(value="会议时长分布统计",notes="")
    public Result<List<DistributionStatisticsVO>> queyMeetingDurationStatistics(@RequestBody StatisticSearchVO statisticSearchVO){
        try{
            if(StringUtils.isEmpty(statisticSearchVO.getType())){
                return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"传参type为空");
            }
            if(!MettingCommonUtil.isExistTimeType(statisticSearchVO.getType().trim())){
                return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"传参type的值有误");
            }
            return ResultUtil.successList(videoMettingService.queyMeetingDurationStatistics(statisticSearchVO));
        }catch(Exception e){
            logger.error("会议时长分布统计异常",e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"会议时长分布统计异常");
        }
    }

    /**
     * 参会人数分布统计
     *
     */
    @PostMapping("participantsStatistics")
    @SysRequestLog(description="参会人数分布统计", actionType = ActionType.SELECT,manually = false)
    @ApiOperation(value="参会人数分布统计",notes="")
    public Result<List<DistributionStatisticsVO>> queyParticipantsStatistics(@RequestBody StatisticSearchVO statisticSearchVO){
        try{
            if(StringUtils.isEmpty(statisticSearchVO.getType())){
                return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"传参type为空");
            }
            if(!MettingCommonUtil.isExistTimeType(statisticSearchVO.getType().trim())){
                return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"传参type的值有误");
            }
            return ResultUtil.successList(videoMettingService.queyParticipantsStatistics(statisticSearchVO));
        }catch(Exception e){
            logger.error("参会人数分布统计异常",e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"参会人数分布统计");
        }
    }
    /**
     * 历史会议列表
     */
    @PostMapping("getPage")
    @SysRequestLog(description="历史会议列表查询", actionType = ActionType.SELECT,manually = false)
    @ApiOperation(value="历史会议列表查询",notes="")
    public PageRes<VideoMettingVO> getPage(@RequestBody VideoMettingSearchVO videoMettingSearchVO){
        return videoMettingService.getPage(videoMettingSearchVO);
    }
    /**
     * 历史会议导出
     */
    @PostMapping("exportData")
    @SysRequestLog(description="生成历史会议列表数据导出文件", actionType = ActionType.EXPORT,manually = false)
    @ApiOperation(value="生成历史会议列表数据导出文件",notes="")
    public Result<String>  exportData(@RequestBody VideoMettingSearchVO videoMettingSearchVO){
        try{
            return videoMettingService.exportData(videoMettingSearchVO);
        }catch(Exception e){
            logger.error("生成历史会议列表数据导出文件异常",e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"生成历史会议列表数据导出文件异常");
        }
    }

    /**
     * 下载历史会议导出文件
     * @param fileName
     * @param response
     */
    @GetMapping(value="/exportFile/{fileName}")
    @SysRequestLog(description="下载历史会议导出文件", actionType = ActionType.EXPORT,manually=false)
    @ApiOperation(value="下载历史会议导出文件",notes="")
    public void exportAssetInfo(@PathVariable  String fileName, HttpServletResponse response){
        videoMettingService.exportAssetInfo(fileName,response);
    }
}
