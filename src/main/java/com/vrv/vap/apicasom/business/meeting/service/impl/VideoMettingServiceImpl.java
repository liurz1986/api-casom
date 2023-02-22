package com.vrv.vap.apicasom.business.meeting.service.impl;

import com.alibaba.fastjson.JSON;
import com.vrv.vap.apicasom.business.meeting.dao.VideoMettingDao;
import com.vrv.vap.apicasom.business.meeting.service.VideoMettingService;
import com.vrv.vap.apicasom.business.meeting.vo.*;
import com.vrv.vap.apicasom.frameworks.config.FileConfiguration;
import com.vrv.vap.exportAndImport.excel.ExcelUtils;
import com.vrv.vap.exportAndImport.excel.util.DateUtils;
import com.vrv.vap.jpa.common.FileUtil;
import com.vrv.vap.jpa.web.Result;
import com.vrv.vap.jpa.web.ResultCodeEnum;
import com.vrv.vap.jpa.web.ResultUtil;
import com.vrv.vap.jpa.web.page.PageRes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.Date;
import java.util.List;

/**
 *视屏会议处理
 * @author liurz
 * @date 2023-2-25
 */
@Service
public class VideoMettingServiceImpl implements VideoMettingService {
    private static Logger logger = LoggerFactory.getLogger(VideoMettingServiceImpl.class);

    @Autowired
    private VideoMettingDao videoMettingDao;

    @Autowired
    private FileConfiguration fileConfiguration;


    /**
     * 会议时长分布统计
     * @param statisticSearchVO
     * @return
     */
    @Override
    public List<DistributionStatisticsVO> queyMeetingDurationStatistics(StatisticSearchVO statisticSearchVO) {
        logger.debug("会议时长分布统计请求参数："+ JSON.toJSONString(statisticSearchVO));
        return videoMettingDao.queyMeetingDurationStatistics(statisticSearchVO);
    }

    /**
     * 参会人数分布统计
     * @param statisticSearchVO
     * @return
     */
    @Override
    public List<DistributionStatisticsVO> queyParticipantsStatistics(StatisticSearchVO statisticSearchVO) {
        logger.debug("参会人数分布统计请求参数："+ JSON.toJSONString(statisticSearchVO));
        return videoMettingDao.queyParticipantsStatistics(statisticSearchVO);
    }

    @Override
    public PageRes<VideoMettingVO> getPage(VideoMettingSearchVO videoMettingSearchVO) {
        logger.debug("历史会议列表查询请求参数："+ JSON.toJSONString(videoMettingSearchVO));
        PageRes<VideoMettingVO> data =new PageRes<>();
        // 获取总数
        long totalNum = videoMettingDao.getPageTotal(videoMettingSearchVO);
        List<VideoMettingVO> list = videoMettingDao.getPageList(videoMettingSearchVO);
        data.setTotal(totalNum);
        data.setList(list);
        data.setMessage(ResultCodeEnum.SUCCESS.getMsg());
        data.setCode(ResultCodeEnum.SUCCESS.getCode().toString());
        return data;
    }

    @Override
    public Result<String> exportData(VideoMettingSearchVO videoMettingSearchVO) {
        logger.debug("历史会议导出请求参数："+ JSON.toJSONString(videoMettingSearchVO));
        String filePath = fileConfiguration.getFilePath();
        List<VideoMettingExportExcelVO> lists = videoMettingDao.exportData(videoMettingSearchVO);
        File newFile= new File(filePath);
        if (!newFile.exists()) {
            newFile.mkdirs();
        }
        String uuid="历史会议列表"+ DateUtils.date2Str(new Date(), "yyyyMMddHHmmss")+".xlsx";
        logger.info("fileName: "+uuid);
        filePath=filePath+ File.separator+uuid;
        try{
            ExcelUtils.getInstance().exportObjects2Excel(lists, VideoMettingExportExcelVO.class, true, filePath);
        }catch(Exception e){
            logger.error("生成历史会议列表数据导出文件导出失败",e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"生成历史会议列表数据导出文件导出失败");
        }
        return ResultUtil.success(uuid);
    }

    @Override
    public void exportAssetInfo(String fileName, HttpServletResponse response) {
        FileUtil.downLoadFile(fileName, fileConfiguration.getFilePath(), response);
    }
}
