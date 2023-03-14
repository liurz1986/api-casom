package com.vrv.vap.apicasom.business.meeting.service.impl;

import com.alibaba.fastjson.JSON;
import com.vrv.vap.apicasom.business.meeting.dao.AbnormalMettingDao;
import com.vrv.vap.apicasom.business.meeting.service.AbnormalMettingService;
import com.vrv.vap.apicasom.business.meeting.util.MettingCommonUtil;
import com.vrv.vap.apicasom.business.meeting.vo.*;
import com.vrv.vap.apicasom.frameworks.config.FileConfiguration;
import com.vrv.vap.exportAndImport.excel.ExcelUtils;
import com.vrv.vap.exportAndImport.excel.util.DateUtils;
import com.vrv.vap.jpa.common.FileUtil;
import com.vrv.vap.jpa.web.Result;
import com.vrv.vap.jpa.web.ResultCodeEnum;
import com.vrv.vap.jpa.web.ResultUtil;
import com.vrv.vap.jpa.web.page.PageRes;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 会议异常记录处理
 *
 * @author liurz
 * @Date 202302
 */
@Service
public class AbnormalMettingServiceImpl implements AbnormalMettingService {
    private static Logger logger = LoggerFactory.getLogger(AbnormalMettingServiceImpl.class);

    @Autowired
    private AbnormalMettingDao abnormalMettingDao;

    @Autowired
    private FileConfiguration fileConfiguration;
    /**
     * 异常类型分布统计
     * @param statisticSearchVO
     * @return
     */
    @Override
    public List<DistributionStatisticsVO> typeStatistics(StatisticSearchVO statisticSearchVO) {
        logger.debug("异常类型分布统计请求参数："+ JSON.toJSONString(statisticSearchVO));
        return abnormalMettingDao.typeStatistics(statisticSearchVO);
    }
    /**
     * 异常严重等级分布统计
     * @param statisticSearchVO
     * @return
     */
    @Override
    public List<DistributionStatisticsVO> gradeStatistics(StatisticSearchVO statisticSearchVO) {
        logger.debug("异常严重等级分布统计请求参数："+ JSON.toJSONString(statisticSearchVO));
        return abnormalMettingDao.gradeStatistics(statisticSearchVO);
    }

    /**
     * 异常趋势统计
     * @param statisticSearchVO
     * @return
     */
    @Override
    public List<AbnormalMettingTrendVO> trendStatistics(StatisticSearchVO statisticSearchVO) throws ParseException {
        logger.debug("异常趋势统计请求参数："+ JSON.toJSONString(statisticSearchVO));
        String type = statisticSearchVO.getType();
        switch (type) {
            case "month":
                return getTreandStatisticsMonth(statisticSearchVO);
            case "halfyear":
                return getTreandStatisticsHalfyear(statisticSearchVO);
            case "year":
                return getTreandStatisticsYear(statisticSearchVO);
            case "none":
                return getTreandStatisticsNone(statisticSearchVO);
            default:
                break;
        }
        return null;
    }
    /**
     * 按月统计,按天统计
     * @param statisticSearchVO
     * @return
     */
    private List<AbnormalMettingTrendVO> getTreandStatisticsMonth(StatisticSearchVO statisticSearchVO) throws ParseException {
        List<String> dataXS = MettingCommonUtil.getMonthDataX();
        List<AbnormalMettingTrendVO> datas = abnormalMettingDao.getTreandStatistics(statisticSearchVO);
        return dataSupplement(datas,dataXS);
    }

    /**
     * 按半年统计,按月统计
     * @param statisticSearchVO
     * @return
     */
    private List<AbnormalMettingTrendVO> getTreandStatisticsHalfyear(StatisticSearchVO statisticSearchVO) {
        List<String> dataXS = MettingCommonUtil.getHalfyearDataX();
        List<AbnormalMettingTrendVO> datas = abnormalMettingDao.getTreandStatistics(statisticSearchVO);
        return dataSupplement(datas,dataXS);
    }

    /**
     * 进一年，按月统计
     * @param statisticSearchVO
     * @return
     */
    private List<AbnormalMettingTrendVO> getTreandStatisticsYear(StatisticSearchVO statisticSearchVO) {
        List<String> dataXS = MettingCommonUtil.getYearDataX();
        List<AbnormalMettingTrendVO> datas = abnormalMettingDao.getTreandStatistics(statisticSearchVO);
        return dataSupplement(datas,dataXS);
    }

    /**
     * 手动输入：
     * 大于24H按天，小于24H按小时
     * @param statisticSearchVO
     * @return
     */
    private List<AbnormalMettingTrendVO> getTreandStatisticsNone(StatisticSearchVO statisticSearchVO) throws ParseException {
        // 开始时间与结束相差大于24H，按天统计，否则按小时统计
        boolean result = MettingCommonUtil.isDay(statisticSearchVO.getEndDate(),statisticSearchVO.getStartDate());
        List<String> dataXs = getDatax(result,statisticSearchVO);
        List<AbnormalMettingTrendVO> datas = abnormalMettingDao.getTreandStatistics(statisticSearchVO);
        return dataSupplement(datas,dataXs);
    }

    private List<String> getDatax(boolean result, StatisticSearchVO statisticSearchVO) throws ParseException {
        if(result){
            return MettingCommonUtil.getDataXByDay(statisticSearchVO.getEndDate(),statisticSearchVO.getStartDate());
        }else{
            return MettingCommonUtil.getDataXByHour(statisticSearchVO.getEndDate(),statisticSearchVO.getStartDate());
        }
    }

    /**
     * 数据补全
     * @param datas
     * @param dataXs
     * @return
     * @throws ParseException
     */
    private  List<AbnormalMettingTrendVO> dataSupplement(List<AbnormalMettingTrendVO> datas ,List<String> dataXs) {
        List<AbnormalMettingTrendVO> allDatas = new ArrayList<AbnormalMettingTrendVO>();
        AbnormalMettingTrendVO data = null;
        for(int i= 0;i < dataXs.size(); i++){
            data = new AbnormalMettingTrendVO();
            setDateY(datas,data,dataXs.get(i));
            allDatas.add(data);
        }
        return allDatas;
    }
    private void setDateY(List<AbnormalMettingTrendVO> datas, AbnormalMettingTrendVO data,String dataX) {
        int num = getNum(datas,dataX);
        data.setDataY(num);
        data.setDataX(dataX);
    }

    private int getNum(List<AbnormalMettingTrendVO> datas, String dataX) {
        if(CollectionUtils.isEmpty(datas)){
            return 0;
        }
        List<AbnormalMettingTrendVO> filters = datas.stream().filter(item -> item.getDataX().equals(dataX)).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(filters)){
            return 0;
        }
        return  filters.get(0).getDataY();
    }

    /**
     * 会议异常记录查询
     *
     * @param abnormalMettingSearchVO
     * @return
     */
    @Override
    public PageRes<AbnormalMettingVO> getPage(AbnormalMettingSearchVO abnormalMettingSearchVO) {
        logger.debug("会议异常记录查询请求参数："+ JSON.toJSONString(abnormalMettingSearchVO));
        PageRes<AbnormalMettingVO> data =new PageRes<>();
        // 获取总数
        long totalNum = abnormalMettingDao.getPageTotal(abnormalMettingSearchVO);
        List<AbnormalMettingVO> list = abnormalMettingDao.getPageList(abnormalMettingSearchVO);
        data.setTotal(totalNum);
        data.setList(list);
        data.setMessage(ResultCodeEnum.SUCCESS.getMsg());
        data.setCode(ResultCodeEnum.SUCCESS.getCode().toString());
        return data;
    }

    /**
     * 生成会议异常记录导出文件
     *
     * @param abnormalMettingSearchVO
     * @return
     */
    @Override
    public Result<String> exportData(AbnormalMettingSearchVO abnormalMettingSearchVO) {
        logger.debug("会议异常记录导出请求参数："+ JSON.toJSONString(abnormalMettingSearchVO));
        String filePath = fileConfiguration.getFilePath();
        List<AbnormalMettingExportExcelVO> lists = abnormalMettingDao.exportData(abnormalMettingSearchVO);
        File newFile= new File(filePath);
        if (!newFile.exists()) {
            newFile.mkdirs();
        }
        String uuid="会议异常记录"+ DateUtils.date2Str(new Date(), "yyyyMMddHHmmss");
        logger.info("uuid: "+uuid);
        String fileName = uuid + ".xls";
        filePath=filePath+ File.separator+fileName;
        try{
            String templatePath= fileConfiguration.getTemplatePath()+"/abnoraml_metting_template.xls";
            logger.info("会议异常记录模板路径: "+templatePath);
            Map<String, String> extenddata = new HashMap<>();
            extenddata.put("title", "会议异常记录");
            ExcelUtils.getInstance().exportObjects2Excel(templatePath,lists,extenddata, AbnormalMettingExportExcelVO.class, false, filePath);
        }catch(Exception e){
            logger.error("生成会议异常记录导出文件导出失败",e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"生成会议异常记录导出文件导出失败");
        }
        return ResultUtil.success(uuid);
    }

    /**
     * 下载生成会议异常记录导出文件
     * @param fileName
     * @param response
     */
    @Override
    public void exportAssetInfo(String fileName, HttpServletResponse response) {
        FileUtil.downLoadFile(fileName+ ".xls", fileConfiguration.getFilePath(), response);

    }

}
