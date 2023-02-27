package com.vrv.vap.apicasom.business.meeting.service.impl;

import com.alibaba.fastjson.JSON;
import com.vrv.vap.apicasom.business.meeting.dao.AbnormalMettingDao;
import com.vrv.vap.apicasom.business.meeting.dao.AccessNodeDao;
import com.vrv.vap.apicasom.business.meeting.dao.VideoMettingDao;
import com.vrv.vap.apicasom.business.meeting.service.LargeScreenService;
import com.vrv.vap.apicasom.business.meeting.util.MeetingConstrant;
import com.vrv.vap.apicasom.business.meeting.util.MettingCommonUtil;
import com.vrv.vap.apicasom.business.meeting.vo.*;
import com.vrv.vap.apicasom.frameworks.util.RedisUtils;
import com.vrv.vap.jpa.common.DateUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 会议大屏
 * @author liurz
 */
@Service
public class LargeScreenServiceImpl implements LargeScreenService {
    private static Logger logger = LoggerFactory.getLogger(LargeScreenServiceImpl.class);

    @Autowired
    private RedisUtils  redisUtils;
    @Autowired
    private AccessNodeDao accessNodeDao;
    @Autowired
    private VideoMettingDao videoMettingDao;
    @Autowired
    private AbnormalMettingDao abnormalMettingDao;
    /**
     * 基本信息：会议视屏节点总数、当前节点在线总数、举办会议次数、参会总人数、会议总时长
     *  会议总时长 单位小时，取整，四舍五入
     *  type:quarter(季)，halfyear(半年)、year(一年)
     * @param type
     * @return
     */
    @Override
    public LargeScreenBaseMessageVO queryBaseMessage(String type) {
        LargeScreenBaseMessageVO data = new LargeScreenBaseMessageVO();
        // 会议视屏节点总数
        int meetingTotal = getMeetingTotal();
        data.setMeetingTotal(meetingTotal);
        // 当前节点在线总数
        int onlineNoeTotal = videoMettingDao.getOnLineNodes();
        data.setOnlineNodeTotal(onlineNoeTotal);
        // 举办会议次数:状态为OFFLINE
        int offlineMettingCount = videoMettingDao.getOffLineMettingTotal(type);
        data.setOfflineMettingCount(offlineMettingCount);
        // 参会总人数:状态为OFFLINE
        int offlineMettingUserCount = videoMettingDao.getOfflineMettingUserCount(type);
        data.setOfflineMettingUserCount(offlineMettingUserCount);
        // 会议总时长: 状态为OFFLINE
        int meetingTimeTotal= videoMettingDao.getOfflineMeetingTimeTotal(type);
        data.setMeetingTimeTotal(meetingTimeTotal);
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
     * 地图查询：
     * type:quarter(季)，halfyear(半年)、year(一年)
     * @return Result
     */
    @Override
    public List<LargeMapVO> queryMapMesage(String type) {
        List<LargeMapVO> result = new ArrayList<>();
        // 城市、组织机构、节点分组统计
        List<CommonQueryVO> groupByCitys = accessNodeDao.queryNodesGroupByCity(type);
        if(CollectionUtils.isEmpty(groupByCitys)){
            return result;
        }
        logger.debug("城市、组织机构、节点分组统计:"+JSON.toJSONString(groupByCitys));
        // 获取存在异常会议的城市、组织机构、节点
        List<CommonQueryVO> abnormalCitys = abnormalMettingDao.getAbnormalMettingCitys(type);
        logger.debug("获取存在异常会议的城市:"+JSON.toJSONString(abnormalCitys));
        // 获取正在开会的城市
        List<String> runCitys = accessNodeDao.getRunMettingCitys(type);
        logger.debug("获取正在开会的城市:"+JSON.toJSONString(runCitys));
        // 数据组合处理
        result = dataHandle(groupByCitys,abnormalCitys,runCitys);
        return result;
    }


    /**
     * 数据处理
     * @param groupByCitys
     * @param abnormalCitys
     * @param runCitys
     * @return
     */
    private List<LargeMapVO> dataHandle(List<CommonQueryVO> groupByCitys, List<CommonQueryVO> abnormalCitys, List<String> runCitys) {
        List<LargeMapVO> result = new ArrayList<>();
        // 按城市分组
        Map<String,List<CommonQueryVO>> groupLists= groupByCitys.stream().collect(Collectors.groupingBy(CommonQueryVO::getKey));
        Set<String> citys = groupLists.keySet();
        LargeMapVO largeMapVO = null;
        // number,city
        for(String city : citys){
            largeMapVO = new LargeMapVO();
            largeMapVO.setCity(city);
            List<CommonQueryVO> nodes = groupLists.get(city);
            largeMapVO.setNodes(nodes.size());
            // 状态处理
            statusHandle(largeMapVO,city,abnormalCitys,runCitys);
            result.add(largeMapVO);
        }
        return result;
    }

    private void statusHandle(LargeMapVO largeMapVO, String city, List<CommonQueryVO> abnormalCitys, List<String> runCitys) {
        CommonQueryVO abnormal = isExistAbnormal(city,abnormalCitys);
        if(null != abnormal){
            // 存在异常
            largeMapVO.setStatus(MeetingConstrant.CITY_STATUS_ABNORMAL);
            // 异常信息描述：组织机构+节点名称+“"异常告警！”
            largeMapVO.setAbnormalMessge(abnormal.getValue()+abnormal.getExtend()+"异常告警！");
        }else{
            boolean isRun = isExistRun(city,runCitys);
            if(isRun){
                // 运行中
                largeMapVO.setStatus(MeetingConstrant.CITY_STATUS_RUN);
            }else{
                // 不存在异常会议和正在进行的会议
                largeMapVO.setStatus(MeetingConstrant.CITY_STATUS_NONE);
            }
        }
    }

    /**
     * 异常会议确认
     * @param city
     * @param abnormalCitys
     * @return
     */
    private CommonQueryVO isExistAbnormal(String city, List<CommonQueryVO> abnormalCitys) {
        if(CollectionUtils.isEmpty(abnormalCitys)){
            return null;
        }
        for(CommonQueryVO abnormalCity : abnormalCitys){
            if(city.equals(abnormalCity.getKey())){
                return abnormalCity;
            }
        }
        return null;
    }

    /**
     * 进行中会议
     * @param city
     * @param abnormalCitys
     * @return
     */
    private boolean isExistRun(String city, List<String> abnormalCitys) {
        if(CollectionUtils.isEmpty(abnormalCitys)){
            return false;
        }
        for(String abnormalCity : abnormalCitys){
            if(city.equals(abnormalCity)){
                return true;
            }
        }
        return false;
    }

    /**
     * 查询城市详情
     *
     * @param commonSearchVO
     * @return
     */
    @Override
    public LargeMapDetailVO queryCityDetail(CommonSearchVO commonSearchVO) {
        LargeMapDetailVO largeMapDetailVO = new LargeMapDetailVO();
        String type = commonSearchVO.getType();
        String cityName = commonSearchVO.getCityName();
        // 当前城市所有节点名称:组织机构和节点名称分组
        List<KeyValueQueryVO> nodeNames = accessNodeDao.queryNodeNamesByCity(type,cityName);
        if(CollectionUtils.isEmpty(nodeNames)){
            return largeMapDetailVO;
        }
        // 城市下节点总数
        largeMapDetailVO.setNodeTotal(nodeNames.size());
        // 当前城市正在开会的节点信息
        List<NodeVO> runNodeVos = accessNodeDao.queryRunNodesByCity(type,cityName);
        // 城市下运行节点总数
        largeMapDetailVO.setRunNodeTotal(runNodeVos.size());
        cityDetailHandle(nodeNames,runNodeVos,largeMapDetailVO);
        return largeMapDetailVO;
    }



    private void cityDetailHandle(List<KeyValueQueryVO> nodeNames,List<NodeVO> runNodeVos, LargeMapDetailVO largeMapDetailVO) {
        List<LargeOrgVO> orgs = new ArrayList<>();
        // 组织机构分组
        Map<String,List<KeyValueQueryVO>> groupLists= nodeNames.stream().collect(Collectors.groupingBy(KeyValueQueryVO::getKey));
        Set<String> keys = groupLists.keySet();
        LargeOrgVO orgVO = null;
        for(String key : keys){
            orgVO = new LargeOrgVO();
            List<KeyValueQueryVO> nodeVOs = groupLists.get(key);
            orgVO.setOrgName(key);
            List<LargeNodeVO> largeNodes = getLargeNodeVOs(runNodeVos,nodeVOs);
            orgVO.setNodes(largeNodes);
            orgs.add(orgVO);
        }
        largeMapDetailVO.setOrgs(orgs);
    }

    private List<LargeNodeVO> getLargeNodeVOs(List<NodeVO> runNodeVos,List<KeyValueQueryVO> nodeVOs) {
        List<LargeNodeVO> nodes = new ArrayList<>();
        LargeNodeVO largeNodeVO = null;
        for(KeyValueQueryVO commonQueryVO : nodeVOs){
            largeNodeVO = new LargeNodeVO();
            NodeVO nodeVO = isRunNode(commonQueryVO.getValue(),runNodeVos);
            addMeetingTime(nodeVO,largeNodeVO);
            largeNodeVO.setNodeName(commonQueryVO.getValue());
            nodes.add(largeNodeVO);
        }
        return nodes;
    }

    private void addMeetingTime(NodeVO nodeVO, LargeNodeVO largeNodeVO) {
        if(null == nodeVO){
            return ;
        }
        Date startTime = nodeVO.getStartTime();
        Date endTime = nodeVO.getEndTime();
        String startStr = " ";
        if(null != startTime){
            startStr = DateUtil.format(startTime,"HH:mm");
        }
        String endTimeStr = " ";
        if(null != endTime){
            endTimeStr = DateUtil.format(endTime,"HH:mm");
        }
        String meetingTime = startStr+"-"+endTimeStr;
        largeNodeVO.setMeetingTime(meetingTime);
    }

    private NodeVO isRunNode(String name, List<NodeVO> runNodeNames) {
        if(CollectionUtils.isEmpty(runNodeNames)){
            return null;
        }
        for(NodeVO nodeVO : runNodeNames) {
            if (name.contains(nodeVO.getName())) {
              return nodeVO;
            }
        }
        return null;
    }
    /**
     * 各地区系统使用统计
     * type:quarter(季)，halfyear(半年)、year(一年)
     * 1. hw_meeting_attendee、hw_meeting_participant关联查询
     * @return Result
     */
    @Override
    public List<LargeBranchStatisticsVO> queryBranchStatistics(String type) {
        return accessNodeDao.queryBranchStatistics(type);
    }

    /**
     * 点对点会议次数、各地区使用占比
     * 各地区使用占比 展示前5的数据，还剩的用其他统计
     * type:quarter(季)，halfyear(半年)、year(一年)
     * @return Result
     */
    @Override
    public LargeBranchUseScaleStatisticsVO queryBranchScaleStatistics(String type) {
        LargeBranchUseScaleStatisticsVO largeBranchUseScaleStatisticsVO = new LargeBranchUseScaleStatisticsVO();
        // 点对点会议次数，历史数据
        int count = videoMettingDao.getPointToPoint(type);
        largeBranchUseScaleStatisticsVO.setPointNum(count);
        // 各地区使用占比,历史数据
        // 地区分组，次数前五的数据,
        List<LargeDeatailVO> list = accessNodeDao.getUseStatisticsByBranch(type);
        if(CollectionUtils.isEmpty(list)){
            largeBranchUseScaleStatisticsVO.setDetail(new ArrayList<>());
            return largeBranchUseScaleStatisticsVO;
        }
        // 节点会议总次数
        int totalCount = accessNodeDao.getUseStatisticsTotalCount(type);
        // 占比处理
        percentHandle(list,totalCount);
        largeBranchUseScaleStatisticsVO.setDetail(list);
        return largeBranchUseScaleStatisticsVO;
    }

    private void percentHandle(List<LargeDeatailVO> list, int totalCount) {
        int sum =0;
        for(LargeDeatailVO data : list){
            sum = sum+ data.getCount();
        }
        int other=  totalCount - sum;
        if(other <= 0){
            return;
        }
        LargeDeatailVO otherDate = new LargeDeatailVO();
        otherDate.setName("其他");
        otherDate.setCount(other);
        list.add(otherDate);
    }

    /**
     * 异常及故障情况分析
     * 异常及故障情况分析 展示前5的数据，还剩的用其他统计
     * @param type
     * @return
     */
    @Override
    public List<LargeDeatailVO> queryBranchAbnormalStatistics(String type) {
        // 异常及故障情况分析:异常
        // 异常名称分组，次数前五的数据
        List<LargeDeatailVO> list = abnormalMettingDao.getStatisticsByName(type);
        if(CollectionUtils.isEmpty(list)){
            return  new ArrayList<>();
        }
        // 异常总数
        int totalCount = abnormalMettingDao.getHistoryTotalCount(type);
        // 占比处理
        percentHandle(list,totalCount);
        return list;
    }

    /**
     * 开会次数
     * @param type
     * @return
     */
    @Override
    public List<LargeDeatailVO> queryNodeMeetingCountStatistics(String type) {
        return accessNodeDao.queryNodeMeetingCountStatistics(type);
    }
    /**
     * 对外提供服务
     * @param type
     * @return
     */
    @Override
    public List<LargeDeatailVO> queryOutServiceStatistics(String type) {
        return accessNodeDao.queryOutServiceStatistics(type);
    }

    /**
     * 多点会议次数
     * @param type
     * @return
     */
    @Override
    public int getManyPoint(String type) {
        // 多点会议次数，历史数据
        int count = videoMettingDao.getManyPoint(type);
        return count;
    }

}
