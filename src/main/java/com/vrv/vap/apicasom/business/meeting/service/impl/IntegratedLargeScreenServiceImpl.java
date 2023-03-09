package com.vrv.vap.apicasom.business.meeting.service.impl;

import com.vrv.vap.apicasom.business.meeting.dao.IntegratedLargeScreenDao;
import com.vrv.vap.apicasom.business.meeting.dao.LargeScreenDao;
import com.vrv.vap.apicasom.business.meeting.service.IntegratedLargeScreenService;
import com.vrv.vap.apicasom.business.meeting.util.MeetingConstrant;
import com.vrv.vap.apicasom.business.meeting.util.MettingCommonUtil;
import com.vrv.vap.apicasom.business.meeting.vo.IntegratedLargeSearchVO;
import com.vrv.vap.apicasom.business.meeting.vo.IntegratedlsBaseVO;
import com.vrv.vap.apicasom.business.meeting.vo.KeyValueQueryVO;
import com.vrv.vap.apicasom.business.task.service.ZkyUnitService;
import com.vrv.vap.apicasom.frameworks.util.RedisUtils;
import com.vrv.vap.common.utils.DateUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 综合大屏
 * @author liurz
 */
@Service
public class IntegratedLargeScreenServiceImpl implements IntegratedLargeScreenService {
    private static Logger logger = LoggerFactory.getLogger(IntegratedLargeScreenServiceImpl.class);
    @Autowired
    private ZkyUnitService zkyUnitService;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private LargeScreenDao largeScreenDao;
    @Autowired
    private IntegratedLargeScreenDao integratedLargeScreenDao;
    /**
     * 基本信息展示(视频会议系统节点数量、当前在线节点数、当前开会节点数、参会总人数、会议总时长、开会次数)
     * @param searchVO
     * @return
     */
    @Override
    public IntegratedlsBaseVO queryBaseMessage(IntegratedLargeSearchVO searchVO) {
        IntegratedlsBaseVO data = new IntegratedlsBaseVO();
        // 视频会议系统节点数量: 取自redis中MettingRooms的值
        int meetingTotal = getMeetingTotal();
        data.setVideoNodeCount(meetingTotal);
        logger.debug("视频会议系统节点数量,来自redis中数据:"+meetingTotal);
        // 当前在线节点数
        int onlineNoeTotal = largeScreenDao.getOnLineNodes();
        data.setOnlineNodeTotal(onlineNoeTotal);
        // 当前开会节点数:状态为在线的会议记录数
        int onLineMettindCount = integratedLargeScreenDao.onLineMettingCount();
        data.setCurrMettingCount(onLineMettindCount);
        // 参会总人数:状态为OFFLINE
        int offlineMettingUserCount = integratedLargeScreenDao.getOfflineMettingUserCount(searchVO);
        data.setMettingUserCount(offlineMettingUserCount);
        // 会议总时长: 状态为OFFLINE
        int meetingTimeTotal= integratedLargeScreenDao.getOfflineMeetingTimeTotal(searchVO);
        data.setMeetingTimeTotal(meetingTimeTotal);
        // 开会次数: 状态为OFFLINE
        int offlineMettingCount = integratedLargeScreenDao.getOffLineMettingTotal(searchVO);
        data.setMeetingTimes(offlineMettingCount);
        // 发送文件: 写死
        data.setSendFiles(0);
        // 接收文件: 写死
        data.setReceiveFiles(0);
        return data;
    }

    /**
     * 会议视屏节点总数:通过redis获取
     * @return
     */
    private int getMeetingTotal() {
        try{
            Object total = redisUtils.get("MeetingRooms");
            if(null == total){
                logger.error("会议视屏节点总数,通过redis获取异常,redis没有该值");
                return 0;
            }
            return  Integer.parseInt(String.valueOf(total));
        }catch (Exception e){
            logger.error("会议视屏节点总数,通过redis获取异常",e);
            return 0;
        }
    }

    /**
     * 接入节点总数:zky_unit表中总数量
     * @return
     */

    @Override
    public long queryNodes() {

        return zkyUnitService.count();
    }

    /**
     * 各地区接入节点分布
     *
     * 接入节点表状态为历史，按分院分组统计数量
     * @return
     */
    @Override
    public List<KeyValueQueryVO> queryBranchNodeStatistics() {
        return integratedLargeScreenDao.queryBranchNodeStatistics();
    }

    /**
     * 应用使用态势
     *
     * 会议记录表状态为历史，时间段分组统计次数
     * @return Result
     */

    @Override
    public List<KeyValueQueryVO> queryMettingsStatistics(IntegratedLargeSearchVO searchVO) throws ParseException {
        Date beginDate = searchVO.getBeginTime();
        Date endDate = searchVO.getEndTime();
        // 判断时间状态
        String status = judgeDateStatus(beginDate,endDate);
        switch (status) {
            case "1":
                return getTreandStatisticsHour(beginDate,endDate);
            case "2":
                return getTreandStatisticsDay(beginDate,endDate);
            case "3":
                return getTreandStatisticsMonth(beginDate,endDate);
            default:
                break;
        }
        List<KeyValueQueryVO> list = new ArrayList<>();
        KeyValueQueryVO data = new KeyValueQueryVO();
        list.add(data);
        return list;
    }

    /**
     * X抽 按小时统计
     * @param beginDate
     * @param endDate
     * @return
     */
    private List<KeyValueQueryVO> getTreandStatisticsHour(Date beginDate, Date endDate) throws ParseException {
        List<String> dataXs= getDataXByHour(endDate,beginDate);
        List<KeyValueQueryVO> list = integratedLargeScreenDao.getTreandStatistics(endDate,beginDate,MeetingConstrant.HOUR_Y);
        return dataSupplement(list,dataXs);
    }

    /**
     * 按小时组装X轴
     * 时间格式：yyyy-MM-dd HH:mm:ss
     * @param endDate
     * @param startDate
     * @return
     * @throws ParseException
     */
    private  List<String> getDataXByHour(Date endDate,Date startDate) throws ParseException {
        List<String> dataXS = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH");
        long startTimes = startDate.getTime();
        long endTimes = endDate.getTime();
        while(startTimes <= endTimes){
            Date date = new Date(startTimes);
            dataXS.add(sdf.format(date));
            startTimes = startTimes+1000 * 60 * 60;
        }
        return dataXS;
    }


    /**
     * X抽 按天统计
     * @param beginDate
     * @param endDate
     * @return
     */
    private List<KeyValueQueryVO> getTreandStatisticsDay(Date beginDate, Date endDate) {
        List<String> dataXs= MettingCommonUtil.getDataXByDay(endDate,beginDate);
        List<KeyValueQueryVO> list = integratedLargeScreenDao.getTreandStatistics(endDate,beginDate,MeetingConstrant.DAY_Y);
        return dataSupplement(list,dataXs);
    }

    /**
     * X抽 按月统计
     * @param beginDate
     * @param endDate
     * @return
     */
    private List<KeyValueQueryVO> getTreandStatisticsMonth(Date beginDate, Date endDate) throws ParseException {
        List<String> dataXs= MettingCommonUtil.getMonthDataX(endDate,beginDate);
        List<KeyValueQueryVO> list = integratedLargeScreenDao.getTreandStatistics(endDate,beginDate,MeetingConstrant.MONTH_Y);
        return dataSupplement(list,dataXs);
    }

    /**
     * 数据补全
     * @param list
     * @param dataXs
     * @return
     */
    private List<KeyValueQueryVO> dataSupplement(List<KeyValueQueryVO> list, List<String> dataXs) {
        List<KeyValueQueryVO> result = new ArrayList<>();
        KeyValueQueryVO resultData = null;
        for(String data : dataXs){
            resultData = new KeyValueQueryVO();
            resultData.setKey(data);
            resultData.setValue(getYData(list,data));
            result.add(resultData);
        }
        return result;
    }

    private String getYData(List<KeyValueQueryVO> list, String key) {
        if(CollectionUtils.isEmpty(list)){
            return "0";
        }
        for(KeyValueQueryVO data : list){
            if(key.equals(data.getKey())){
                return data.getValue();
            }
        }
        return "0";
    }

    /**
     * 判断时间状态
     * 开始时间-结束时间相差
     *   小于等于24H，按小时统计
     *   大于24H小于等于1个月 ，按天统计
     *    大于1个月，按月统计
     * @param beginDate
     * @param endDate
     * @return
     */
    private String judgeDateStatus(Date beginDate, Date endDate) {
        // 判断是不是大于24H
        boolean is24h = isDay(endDate,beginDate);
        if(!is24h){
            return MeetingConstrant.HOUR_Y;
        }
        // 如果大于24H，判断是不是大于1个月
        boolean isMonth = isMonth(beginDate,endDate);
        if(isMonth){
            return MeetingConstrant.MONTH_Y;
        }
        return MeetingConstrant.DAY_Y;

    }

    /**
     * 判断是不是大于24小时：
     * 时间格式为：yyyy-MM-dd HH:mm:ss
     * 24小时换算成毫秒：24*60*60*1000
     * @param endDate
     * @param startDate
     * @return
     */
    private boolean isDay(Date endDate,Date startDate) {
        long endtime = endDate.getTime();
        long starttime= startDate.getTime();
        long result = endtime-starttime;
        // 24小时换算成毫秒
        long hour= 24*60*60*1000;
        // 大于24小时按天
        if(result > hour){
            return true;
        }
        return false;
    }


    /**
     * 两个时间相差是否大于一个月
     * @param beginDate
     * @param endDate
     * @return
     */
    private boolean isMonth(Date beginDate, Date endDate) {
        // 开始时间向前推一个月
        Date monthDate = DateUtils.addMonths(beginDate,1);
        if(endDate.getTime() > monthDate.getTime()){
            return true;
        }
        return false;
    }
}
