package com.vrv.vap.apicasom.business.meeting.controller;
import com.vrv.vap.apicasom.business.meeting.service.AbnormalMettingService;
import com.vrv.vap.apicasom.business.meeting.util.MettingCommonUtil;
import com.vrv.vap.apicasom.business.meeting.vo.*;
import com.vrv.vap.exportAndImport.excel.util.DateUtils;
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
 * 异常会议
 */
@RestController
@RequestMapping("abnormalMetting")
public class AbnormalMettingController {
    private static Logger logger = LoggerFactory.getLogger(AbnormalMettingController.class);

    @Autowired
    private AbnormalMettingService abnormalMettingService;

    /**
     * 异常类型分布统计
     *
     */
    @PostMapping("typeStatistics")
    @SysRequestLog(description="异常类型分布统计", actionType = ActionType.SELECT,manually = false)
    @ApiOperation(value="异常类型分布统计",notes="")
    public Result<List<DistributionStatisticsVO>> typeStatistics(@RequestBody StatisticSearchVO statisticSearchVO){
        try{
            if(StringUtils.isEmpty(statisticSearchVO.getType())){
                return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"传参type为空");
            }
            if(!MettingCommonUtil.isExistTimeType(statisticSearchVO.getType().trim())){
                return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"传参type的值有误");
            }
            return ResultUtil.successList(abnormalMettingService.typeStatistics(statisticSearchVO));
        }catch(Exception e){
            logger.error("异常类型分布统计异常",e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"异常类型分布统计异常");
        }
    }

    /**
     * 异常严重等级分布统计
     *
     */
    @PostMapping("gradeStatistics")
    @SysRequestLog(description="异常严重等级分布统计", actionType = ActionType.SELECT,manually = false)
    @ApiOperation(value="异常严重等级分布统计",notes="")
    public Result<List<DistributionStatisticsVO>> gradeStatistics(@RequestBody StatisticSearchVO statisticSearchVO){
        try{
            if(StringUtils.isEmpty(statisticSearchVO.getType())){
                return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"传参type为空");
            }
            if(!MettingCommonUtil.isExistTimeType(statisticSearchVO.getType().trim())){
                return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"传参type的值有误");
            }
            return ResultUtil.successList(abnormalMettingService.gradeStatistics(statisticSearchVO));
        }catch(Exception e){
            logger.error("异常严重等级分布统计异常",e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"异常严重等级分布统计异常");
        }
    }
    /**
     * 异常趋势统计
     *
     * 月：按天统计
     * 半年：按月统计
     * 年：按月统计
     */
    @PostMapping("trendStatistics")
    @SysRequestLog(description="异常趋势统计", actionType = ActionType.SELECT,manually = false)
    @ApiOperation(value="异常趋势统计",notes="")
    public Result<List<AbnormalMettingTrendVO>> trendStatistics(@RequestBody StatisticSearchVO statisticSearchVO){
        try{
            if(StringUtils.isEmpty(statisticSearchVO.getType())){
                return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"传参type为空");
            }
            if(!MettingCommonUtil.isExistTimeType(statisticSearchVO.getType().trim())){
                return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"传参type的值有误");
            }
            return ResultUtil.successList(abnormalMettingService.trendStatistics(statisticSearchVO));
        }catch(Exception e){
            logger.error("异常趋势统计异常",e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"异常趋势统计异常");
        }
    }
    /**
     * 会议异常记录查询
     */
    @PostMapping("getPage")
    @SysRequestLog(description="会议异常记录查询", actionType = ActionType.SELECT,manually = false)
    @ApiOperation(value="会议异常记录查询",notes="")
    public PageRes<AbnormalMettingVO> getPage(@RequestBody AbnormalMettingSearchVO abnormalMettingSearchVO){
        return abnormalMettingService.getPage(abnormalMettingSearchVO);
    }
    /**
     * 会议异常记录导出
     */
    @PostMapping("exportData")
    @SysRequestLog(description="生成会议异常记录导出文件", actionType = ActionType.EXPORT,manually = false)
    @ApiOperation(value="生成会议异常记录导出文件",notes="")
    public Result<String>  exportData(@RequestBody AbnormalMettingSearchVO abnormalMettingSearchVO){
        try{
            return abnormalMettingService.exportData(abnormalMettingSearchVO);
        }catch(Exception e){
            logger.error("生成会议异常记录导出文件异常",e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"生成会议异常记录导出文件异常");
        }
    }

    /**
     * 下下载会议异常记录导出
     * @param fileName
     * @param response
     */
    @GetMapping(value="/exportFile/{fileName}")
    @SysRequestLog(description="下载会议异常记录导出文件", actionType = ActionType.EXPORT,manually=false)
    @ApiOperation(value="下载会议异常记录导出文件",notes="")
    public void exportAssetInfo(@PathVariable  String fileName, HttpServletResponse response){
        abnormalMettingService.exportAssetInfo(fileName,response);
    }
}
