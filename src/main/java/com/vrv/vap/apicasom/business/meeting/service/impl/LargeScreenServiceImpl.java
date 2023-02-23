package com.vrv.vap.apicasom.business.meeting.service.impl;

import com.alibaba.fastjson.JSON;
import com.vrv.vap.apicasom.business.meeting.dao.AbnormalMettingDao;
import com.vrv.vap.apicasom.business.meeting.dao.AccessNodeDao;
import com.vrv.vap.apicasom.business.meeting.dao.VideoMettingDao;
import com.vrv.vap.apicasom.business.meeting.service.LargeScreenService;
import com.vrv.vap.apicasom.business.meeting.util.MeetingConstrant;
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
        // 城市、节点分组统计
        List<CommonQueryVO> groupByCitys = accessNodeDao.queryNodesGroupByCity(type);
        if(CollectionUtils.isEmpty(groupByCitys)){
            return result;
        }
        logger.debug("城市、节点分组统计:"+JSON.toJSONString(groupByCitys));
        // 获取存在异常会议的城市
        List<String> abnormalCitys = abnormalMettingDao.getAbnormalMettingCitys(type);
        logger.debug("获取存在异常会议的城市:"+JSON.toJSONString(abnormalCitys));
        // 获取不存在存在异常会议并且正在开会的城市
        List<String> runCitys = accessNodeDao.getRunMettingCitys(type);
        logger.debug("获取不存在存在异常会议并且正在开会的城市:"+JSON.toJSONString(runCitys));
        // 数据组合处理
        result = dataHandle(groupByCitys,abnormalCitys,runCitys);
        return result;
    }



    private List<LargeMapVO> dataHandle(List<CommonQueryVO> groupByCitys, List<String> abnormalCitys, List<String> runCitys) {
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

    private void statusHandle(LargeMapVO largeMapVO, String city, List<String> abnormalCitys, List<String> runCitys) {
        boolean isAbnormal = isExist(city,abnormalCitys);
        if(isAbnormal){
            // 存在异常
            largeMapVO.setStatus(MeetingConstrant.CITY_STATUS_ABNORMAL);
        }else{
            boolean isRun = isExist(city,runCitys);
            if(isRun){
                // 运行中
                largeMapVO.setStatus(MeetingConstrant.CITY_STATUS_RUN);
            }else{
                // 不存在异常会议和正在进行的会议
                largeMapVO.setStatus(MeetingConstrant.CITY_STATUS_NONE);
            }
        }
    }

    private boolean isExist(String city, List<String> abnormalCitys) {
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
        List<CommonQueryVO> nodeNames = accessNodeDao.queryNodeNamesByCity(type,cityName);
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

    private void cityDetailHandle(List<CommonQueryVO> nodeNames,List<NodeVO> runNodeVos, LargeMapDetailVO largeMapDetailVO) {
        List<LargeOrgVO> orgs = new ArrayList<>();
        // 组织机构分组
        Map<String,List<CommonQueryVO>> groupLists= nodeNames.stream().collect(Collectors.groupingBy(CommonQueryVO::getKey));
        Set<String> keys = groupLists.keySet();
        LargeOrgVO orgVO = null;
        for(String key : keys){
            orgVO = new LargeOrgVO();
            List<CommonQueryVO> nodeVOs = groupLists.get(key);
            orgVO.setOrgName(key);
            List<LargeNodeVO> largeNodes = getLargeNodeVOs(runNodeVos,nodeVOs);
            orgVO.setNodes(largeNodes);
            orgs.add(orgVO);
        }
        largeMapDetailVO.setOrgs(orgs);
    }

    private List<LargeNodeVO> getLargeNodeVOs(List<NodeVO> runNodeVos,List<CommonQueryVO> nodeVOs) {
        List<LargeNodeVO> nodes = new ArrayList<>();
        LargeNodeVO largeNodeVO = null;
        for(CommonQueryVO commonQueryVO : nodeVOs){
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

}
