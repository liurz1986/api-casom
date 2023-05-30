package com.vrv.vap.apicasom.business.meeting.service.impl;

import com.alibaba.fastjson.JSON;
import com.vrv.vap.apicasom.business.meeting.bean.ZkyPrintUserOrg;
import com.vrv.vap.apicasom.business.meeting.cache.LocalCache;
import com.vrv.vap.apicasom.business.meeting.dao.SituationLargeScreenDao;
import com.vrv.vap.apicasom.business.meeting.service.SituationLargeScreenService;
import com.vrv.vap.apicasom.business.meeting.service.ZkyPrintUserOrgService;
import com.vrv.vap.apicasom.business.meeting.util.MettingCommonUtil;
import com.vrv.vap.apicasom.business.meeting.util.SituationLargeScreenUtil;
import com.vrv.vap.apicasom.business.meeting.vo.*;
import com.vrv.vap.common.utils.DateUtils;
import com.vrv.vap.es.enums.FieldType;
import com.vrv.vap.es.service.ElasticSearchMapManage;
import com.vrv.vap.es.util.ElasticSearchUtil;
import com.vrv.vap.es.util.page.QueryCondition_ES;
import com.vrv.vap.es.vo.SearchField;
import com.vrv.vap.jpa.common.DateUtil;
import com.vrv.vap.jpa.web.Result;
import com.vrv.vap.jpa.web.ResultUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.ParsedSum;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 态势应用大屏
 *
 * 2023-5-27
 */

@Service
public class SituationLargeScreenServiceImpl implements SituationLargeScreenService {
    private static Logger logger = LoggerFactory.getLogger(SituationLargeScreenServiceImpl.class);
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Autowired
    private ElasticSearchMapManage elasticSearchMapManage;
    @Autowired
    private ZkyPrintUserOrgService zkyPrintUserOrgService;
    @Autowired
    private SituationLargeScreenDao situationLargeScreenDao;
    // 查询打印和刻录的es索引
    private String indexName="summary-user-dk-count-*";
    /**
     * 公文及文件交换系统发件数量、收件数量
     * @param searchVO
     * @return
     */
    @Override
    public FileSendAndReceiveNumVO fileSendAndReceiveNum(SituationLargeSearchVO searchVO) {
        FileSendAndReceiveNumVO result = new FileSendAndReceiveNumVO();
        // 获取发件数量top10
        List<KeyValueQueryVO> sendTop10 = situationLargeScreenDao.fileSendAndReceiveNumTop10("1",searchVO.getType());
        // 获取收件数据top10
        List<KeyValueQueryVO> receviceTop10 = situationLargeScreenDao.fileSendAndReceiveNumTop10("2",searchVO.getType());
        result.setSend(sendTop10);
        result.setReceive(receviceTop10);
        return result;
    }

    /**
     * 发件和收件情况统计
     * 1.本地区收件
     * 2.本地地区发件
     * 3.跨地区收件
     * 4.跨地区发件
     * 月按照天展示
     * 半年、年、所有按月展示
     * 全部的话：根据实际最大时间和最小时间来判断
     *      大于2年按年统计
     *      大于1个月按月统计
     *      小于等于1个月按天统计
     * 全部的话，如果没有数据x轴为空
     * @param searchVO
     * @return
     */
    @Override
    public List<FileSendAndReceiveVO> fileSendAndReceiveTrend(SituationLargeSearchVO searchVO) throws ParseException {
        List<FileSendAndReceiveVO> result = new ArrayList<>();
        String type = searchVO.getType();
        // 一个月按天,半年, 年按月,超过二年按年
        Map<String,Object> xStrengthMap = getXstrength(type);
        String xStrength = String.valueOf(xStrengthMap.get("xstrength"));
        if("-1".equals(xStrength)){
            logger.info("zky_send表没有数据");
            return result;
        }
        // 本地收件
        List<TreandVO> localReceives = situationLargeScreenDao.getFileSendAndReceiveTreandStatistics(xStrength,"1",type);
        // 本地发件件
        List<TreandVO> localSends = situationLargeScreenDao.getFileSendAndReceiveTreandStatistics(xStrength,"2",type);
        // 跨地区发件
        List<TreandVO> noLocalReceives = situationLargeScreenDao.getFileSendAndReceiveTreandStatistics(xStrength,"3",type);
        // 跨地区发件
        List<TreandVO> noLocalSends = situationLargeScreenDao.getFileSendAndReceiveTreandStatistics(xStrength,"4",type);
        // 整个X轴数据
        List<String> dataXs = getDataX(xStrengthMap,type);
        // 数据组装处理
        return dataHandle(dataXs,noLocalSends,noLocalReceives,localSends,localReceives);

    }

    private List<FileSendAndReceiveVO> dataHandle(List<String> dataXs, List<TreandVO> noLocalSends, List<TreandVO> noLocalReceives, List<TreandVO> localSends, List<TreandVO> localReceives) {
        List<FileSendAndReceiveVO> result = new ArrayList<>();
        FileSendAndReceiveVO data = null;
        for(String x : dataXs){
            data = new FileSendAndReceiveVO();
            data.setName(x);
            data.setTransRegionalSendNum(getTrendValue(x,noLocalSends));
            data.setTransRegionalReceiveNum(getTrendValue(x,noLocalReceives));
            data.setLocalSendNum(getTrendValue(x,localSends));
            data.setLocalSendNum(getTrendValue(x,localReceives));
            result.add(data);
        }
        return result;
    }

    private int getTrendValue(String x, List<TreandVO> noLocalSends) {
        if(null == noLocalSends || noLocalSends.size() == 0){
            return 0;
        }
        for(TreandVO data : noLocalSends){
            String dataX = data.getDataX();
            if(x.equals(dataX)){
                return data.getDataY();
            }
        }
        return 0;
    }



    /**
     * 进一个月 ：按天统计 0
     * 半天和年：按月统计 1
     * 全部的话：根据实际最大时间和最小时间来判断
     *         大于2年按年统计 2
     *         大于1个月按月统计 1
     *          小于等于1个月按天统计 0
     * @param type
     * @return
     */
    private Map<String,Object> getXstrength(String type) throws ParseException {
        Map<String,Object> result = new HashMap<>();
        switch (type) {
            case "month":
                result.put("xstrength","0"); // 按天
                return result;
            case "halfyear":
            case "year":
                result.put("xstrength","1");  // 半年和年 按月
                return result;
            case "all": // 全部，根据具体时间判断
                Map<String,Object> times = situationLargeScreenDao.getMaxAndMinStartTime();
                if(null == times || times.size() == 0){
                    result.put("xstrength","-1");  ;// 没有数据
                    return result;
                }
                Object minTime = times.get("minTime");
                String minTimeStr= String.valueOf(minTime);
                Object maxTime = times.get("maxTime");
                String maxTimeStr= String.valueOf(maxTime);
                Date startTime = DateUtils.dateTime(DateUtil.DEFAULT_DATE_PATTERN,minTimeStr);
                Date endTime = DateUtils.dateTime(DateUtil.DEFAULT_DATE_PATTERN,maxTimeStr);
                int month = SituationLargeScreenUtil.monthDiff(startTime,endTime);
                result.put("startTime",startTime);  ;// 开始时间
                result.put("endTime",endTime);  ;// 开始时间
                if(month > 12){
                    result.put("xstrength","2");  ;// 大于1年按年统计
                    return result;
                }
                if(month >=1){
                    result.put("xstrength","1");  // 大于1个月按月统计
                    return result;
                }
                result.put("xstrength","0");  // 小于1个月按天统计
                return result;
        }
        return  result;
    }

    private List<String> getDataX(Map<String,Object> xStrengthMap,String type) throws ParseException {
        String xStrength = String.valueOf(xStrengthMap.get("xstrength"));
        switch (type) {
            case "month":
                return MettingCommonUtil.getMonthDataX();
            case "halfyear":
                return MettingCommonUtil.getHalfyearDataX();
            case "year":
                return MettingCommonUtil.getYearDataX();
            case "all": //全部时
                Date startTime = (Date)xStrengthMap.get("startTime");
                Date endTime = (Date)xStrengthMap.get("endTime");
                // 小于一个月按天
                if("0".equals(xStrength)){
                    return MettingCommonUtil.getDataXByDay(endTime,startTime);
                }
                // 小于一年和大于一个月，按月
                if("1".equals(xStrength)){
                    return MettingCommonUtil.getMonthDataX(endTime,startTime);
                }
                // 大于2年按年统计
                return MettingCommonUtil.getYearDataX(endTime,startTime);
        }
        return null;
    }

    /**
     * 院机关各部门邮件收发数量
     * @return Result
     */
    @Override
    public EmailSendAndReceiveNumVO emailSendAndReceiveNum(SituationLargeSearchVO searchVO) {
        EmailSendAndReceiveNumVO result = new EmailSendAndReceiveNumVO();
        Map<String,Object> totals = situationLargeScreenDao.emailSendAndReceiveTotal(searchVO.getType());
        int receiveNum = totals.get("receiveNum")==null?0:Integer.parseInt(String.valueOf(totals.get("receiveNum")));
        int sendNum = totals.get("sendNum")==null?0:Integer.parseInt(String.valueOf(totals.get("sendNum")));
        if(0 == sendNum && 0 == receiveNum){
            result.setReceiveTotal(0);
            result.setSendTotal(0);
            return result;
        }
        List<KeyValueQueryVO> receiveGroups = situationLargeScreenDao.emailSendAndReceiveNum("1",searchVO.getType());
        List<KeyValueQueryVO> sendGroups = situationLargeScreenDao.emailSendAndReceiveNum("2",searchVO.getType());
        return emailDataHandle(receiveGroups,sendGroups,receiveNum,sendNum);
    }

    private EmailSendAndReceiveNumVO emailDataHandle(List<KeyValueQueryVO> receiveGroups, List<KeyValueQueryVO> sendGroups, int receiveNum, int sendNum) {
        EmailSendAndReceiveNumVO result = new EmailSendAndReceiveNumVO();
        if(receiveNum > 0){
            Map<String,Object> receives = getTOP2(receiveGroups);
            int sum =(Integer) receives.get("sum");
            List<KeyValueQueryVO> list =(List) receives.get("data");
            if(receiveNum > sum){
                KeyValueQueryVO other = new KeyValueQueryVO();
                other.setValue((receiveNum - sum)+"");
                other.setKey("其他");
                list.add(other);
            }
            result.setReceiveTop(list);
            result.setReceiveTotal(receiveNum);
        }
        if(sendNum > 0){
            Map<String,Object> sends =getTOP2(sendGroups);
            int sum =(Integer) sends.get("sum");
            List<KeyValueQueryVO> list =(List) sends.get("data");
            if(sendNum > sum){
                KeyValueQueryVO other = new KeyValueQueryVO();
                other.setValue((sendNum - sum)+"");
                other.setKey("其他");
                list.add(other);
            }
            result.setSendTop(list);
            result.setSendTotal(sendNum);
        }
        return result;
    }

    private Map<String,Object> getTOP2(List<KeyValueQueryVO> receiveGroups) {
        Map<String,Object> tops = new HashMap<>();
        tops.put("sum",0);
        List<KeyValueQueryVO> result = new ArrayList<>();
        int sum = 0;
        int size = receiveGroups.size();
        if(size == 0){
            tops.put("data",new ArrayList<>());
            return tops;
        }
        if(size == 1){
            KeyValueQueryVO data = receiveGroups.get(0);
            sum = data.getValue()==null?0:Integer.parseInt(data.getValue());
            result.add(data);
            tops.put("data",result);
            tops.put("sum",sum);
            return tops;
        }
        if(size > 1){
            KeyValueQueryVO data1 = receiveGroups.get(0);
            int num1 = data1.getValue()==null?0:Integer.parseInt(data1.getValue());
            KeyValueQueryVO data2 = receiveGroups.get(1);
            int num2 = data2.getValue()==null?0:Integer.parseInt(data2.getValue());
            result.add(data1);
            result.add(data2);
            tops.put("data",result);
            tops.put("sum",num1+num2);
        }
        return tops;
    }

    /**
     * 收发件数量
     * tabName "1":各分院(地区)  "2":院机关各部门
     * 1.本地区收件
     * 2.本地地区发件
     * 3.跨地区收件
     * 4.跨地区发件
     * @return Result
     */
    @Override
    public List<FileSendAndReceiveVO>  fileSendAndReceiveTab(SituationLargeSearchVO searchVO, String tabName) {
        List<FileSendAndReceiveVO> result = new ArrayList<>();
        // 各分院(地区)：send_scope为全院
        if("1".equals(tabName)){
            return groupBranchResult(searchVO.getType());
        }
        // 院机关各部门：send_scope为院机关
        if("2".equals(tabName)){
            return groupOrgNameResult(searchVO.getType());
        }
        return result;
    }

    /**
     * 各分院统计
     * @param type
     * @return
     */
    private List<FileSendAndReceiveVO> groupBranchResult(String type) {
        Map<String,FileSendAndReceiveVO> handle = new HashMap<>();
        List<Map<String,Object>> list = situationLargeScreenDao.fileSendAndReceiveBranch(type);
        for(Map<String,Object> data : list){
            String branch = String.valueOf(data.get("branch"));
            String sendRegion = String.valueOf(data.get("sendRegion"));
            String sendType = String.valueOf(data.get("sendType"));
            int receiveNum = data.get("receiveNum") == null?0:Integer.parseInt(String.valueOf(data.get("receiveNum")));
            int sendNum = data.get("sendNum") == null?0:Integer.parseInt(String.valueOf(data.get("sendNum")));
            if(handle.containsKey(branch)){
                FileSendAndReceiveVO oldData = handle.get(branch);
                addData(oldData,sendRegion,sendType,receiveNum,sendNum);
                handle.put(branch,oldData);
            }else{
                FileSendAndReceiveVO vo = new FileSendAndReceiveVO();
                addData(vo,sendRegion,sendType,receiveNum,sendNum);
                vo.setName(branch);
                handle.put(branch,vo);
            }
        }
        Collection<FileSendAndReceiveVO> collections = handle.values();
        List<FileSendAndReceiveVO> resultDatas = collections.stream().collect(Collectors.toList());
        return resultDatas;
    }

    private List<FileSendAndReceiveVO> groupOrgNameResult(String type) {
        Map<String,FileSendAndReceiveVO> handle = new HashMap<>();
        List<Map<String,Object>> list = situationLargeScreenDao.fileSendAndReceiveOrgName(type);
        for(Map<String,Object> data : list){
            String orgName = String.valueOf(data.get("orgName"));
            String sendRegion = String.valueOf(data.get("sendRegion"));
            String sendType = String.valueOf(data.get("sendType"));
            int receiveNum = data.get("receiveNum") == null?0:Integer.parseInt(String.valueOf(data.get("receiveNum")));
            int sendNum = data.get("sendNum") == null?0:Integer.parseInt(String.valueOf(data.get("sendNum")));
            if(handle.containsKey(orgName)){
                FileSendAndReceiveVO oldData = handle.get(orgName);
                addData(oldData,sendRegion,sendType,receiveNum,sendNum);
                handle.put(orgName,oldData);
            }else{
                FileSendAndReceiveVO vo = new FileSendAndReceiveVO();
                addData(vo,sendRegion,sendType,receiveNum,sendNum);
                vo.setName(orgName);
                handle.put(orgName,vo);
            }
        }
        Collection<FileSendAndReceiveVO> collections = handle.values();
        List<FileSendAndReceiveVO> resultDatas = collections.stream().collect(Collectors.toList());
        return resultDatas;
    }

    /**
     * 数据组装
     * sendRegion中1表示跨地区、0表示本地
     * sendType为发件、收件
     * @param data
     * @param sendRegion
     * @param sendType
     * @param receiveNum
     * @param sendNum
     */
    private void addData(FileSendAndReceiveVO data, String sendRegion,String sendType , int receiveNum, int sendNum) {
        // 本地
        if("0".equals(sendRegion)){
            if("发件".equals(sendType)){
                data.setLocalSendNum(sendNum);
            }
            if("收件".equals(sendType)){
                data.setLocalReceiveNum(receiveNum);
            }
        }
        // 跨地区
        if("1".equals(sendRegion)){
            if("发件".equals(sendType)){
                data.setTransRegionalSendNum(sendNum);
            }
            if("收件".equals(sendType)){
                data.setTransRegionalReceiveNum(receiveNum);
            }
        }
    }

    /**
     * 打印和刻录数量 (es中data_time的时间格式为：yyyy-MM-dd)
     * @return Result
     */
    @Override
    public Result<List<PrintingAndBurningNumVO>> printingAndBurningNum(SituationLargeSearchVO searchVO) throws ParseException, IOException {
        String type = searchVO.getType();
        // 获取查询时间条件
        Map<String,String> timeResult = getStartTimeAndEndTimeByTypeES(type,"yyyy-MM-dd");
        // 获取es用户对应数据：0为打印，1为刻录
        List<PrintingAndBurningNumVO> datas=getDatasByES(type,timeResult);
        if(null == datas || datas.size() ==0){
            logger.info("打印和刻录数量查询，es中没有查到数据");
            return ResultUtil.successList(null);
        }
        // 获取用户打印机构数据
        List<ZkyPrintUserOrg> alls = getAllPrintUserOrgs();
        if(null == alls || alls.size() ==0){
            logger.info("打印和刻录数量查询，用户打印机构数据没有数据");
            return ResultUtil.successList(null);
        }
        // 根据es数据和用户打印机构数据组装数据
        return structureData(datas,alls);
    }

    /**
     * 根据es数据和用户打印机构数据组装数据
     * @param datas
     * @param alls
     * @return
     */
    private Result<List<PrintingAndBurningNumVO>> structureData(List<PrintingAndBurningNumVO> datas, List<ZkyPrintUserOrg> alls) {
        Map<String,PrintingAndBurningNumVO> result = new HashMap<>();
        List<String> noExistUserNames = new ArrayList<>();
        List<String> existBranhs = new ArrayList<>();
        for(PrintingAndBurningNumVO vo : datas){
           String userName=  vo.getName();
           int burningNum = vo.getBurningNum();
           int printingNum = vo.getPrintingNum();
           String branch = getBranchByUserName(userName,alls);
           if(null == branch){
               noExistUserNames.add(userName);
               continue;
           }
           if(existBranhs.contains(branch)){
               PrintingAndBurningNumVO his = result.get(branch);
               his.setBurningNum(his.getBurningNum()+burningNum);
               his.setPrintingNum(his.getPrintingNum()+printingNum);
               result.put(branch,his);
           }else{
               PrintingAndBurningNumVO newData = new PrintingAndBurningNumVO();
               newData.setBurningNum(burningNum);
               newData.setPrintingNum(printingNum);
               newData.setName(branch);
               existBranhs.add(branch);
               result.put(branch,newData);
           }
        }
        logger.info("es中用户不在用户打印列表中数据有："+ JSON.toJSONString(noExistUserNames));
        if(result.size() == 0){
            return ResultUtil.successList(new ArrayList<>());
        }
        Collection<PrintingAndBurningNumVO> collections = result.values();
        List<PrintingAndBurningNumVO> list = collections.stream().collect(Collectors.toList());
        return ResultUtil.successList(list);
    }

    private String getBranchByUserName(String userName, List<ZkyPrintUserOrg> alls) {
        for(ZkyPrintUserOrg org : alls){
            String name = org.getUserName();
            if(name.equals(userName)){
                return org.getBranch();
            }
        }
        return null;
    }

    // 获取所有用户打印机构数据：先从缓存拿，没有去查询
    private List<ZkyPrintUserOrg> getAllPrintUserOrgs() {
        List<ZkyPrintUserOrg> cachDatas = LocalCache.getAllZkyPrintUserOrgCache();
        if(CollectionUtils.isEmpty(cachDatas)){
            cachDatas = zkyPrintUserOrgService.findAll();
            LocalCache.upDateAllZkyPrintUserOrgCache(cachDatas);
        }
        return cachDatas;
    }

    /**
     * 通过ES获取数据
     * 1. 通过用户名、op_type 分组求dk_count的和
     * 2. 组装数据
     * @param type
     * @param timeResult
     * @return
     */
   private List<PrintingAndBurningNumVO> getDatasByES(String type, Map<String, String> timeResult){
       List<QueryCondition_ES> conditions = new ArrayList<>();
       if(!"all".equals(type)){
           conditions.add(QueryCondition_ES.in("op_type",new String[]{"0","1"}));
           conditions.add(QueryCondition_ES.between("data_time",timeResult.get("startTime"),timeResult.get("endTime")));
       }
       SearchField child1=new SearchField("dk_count", FieldType.NumberSum, null);
       SearchField child2=new SearchField("op_type", FieldType.String, child1);
       SearchField searchField=new SearchField("username", FieldType.String, child2);
       String indexName = "summary-user-dk-count-*";
       List<Map<String, Object>> datas  = elasticSearchMapManage.queryStatistics(indexName,conditions,searchField);
       List<PrintingAndBurningNumVO> result = new ArrayList<>();
       PrintingAndBurningNumVO user = null;
       for(Map<String, Object> data : datas){
           user = new PrintingAndBurningNumVO();
           String username = String.valueOf(data.get("username"));
           user.setName(username);
           List<Map<String, Object>> opTypeGroupList = (List)data.get("op_type");
           // 打印和刻录
           printingAndBurningHandle(opTypeGroupList,user);
           result.add(user);
       }
       return result;
   }

    private void printingAndBurningHandle(List<Map<String, Object>> opTypeGroupList, PrintingAndBurningNumVO user) {
        for(Map<String, Object> data : opTypeGroupList){
            // 打印和刻录 0为打印，1为刻录
            String optType = String.valueOf(data.get("op_type"));
            List<Map<String, Object>> dkCountMaps = (List)data.get("dk_count");
            int value = getSumValue(dkCountMaps);
            switch (optType){
                case "0":
                    user.setPrintingNum(value);
                    break;
                case "1":
                    user.setBurningNum(value);
                    break;
            }
        }
    }

    private int getSumValue(List<Map<String, Object>> dkCountMaps){
        if(CollectionUtils.isEmpty(dkCountMaps)){
            return 0;
        }
        Object value = dkCountMaps.get(0).get("doc_count");
        if(null == value){
            return 0;
        }
        Float fcount = Float.parseFloat(String.valueOf(value));
        int sumValue = fcount.intValue();
        return sumValue;
    }

    /**
     * 根据时间查询ES数据
     * 然后按照usename、op_type分组，分组后求dk_count的和
     * @param type
     * @param timeResult
     * @param opType 打印或刻录
     * @return
     * @throws IOException
     */
    public Map<String, Object> getEsDatas(String type, Map<String, String> timeResult,String opType) throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // username、opType进行分组
        TermsAggregationBuilder groupByUsernameAgg = AggregationBuilders.terms("groupName").field("username");
        TermsAggregationBuilder groupByopTypeAgg = AggregationBuilders.terms("opType").field("op_type");
        // dk_count求和
        AggregationBuilder sumDKCount = AggregationBuilders.sum("sumCount").field("dk_count");
        // 分组后求和
        groupByopTypeAgg.subAggregation(sumDKCount);
        groupByUsernameAgg.subAggregation(groupByopTypeAgg);
        groupByUsernameAgg.shardSize(1000000);
        // 指定agg
        searchSourceBuilder.aggregation(groupByUsernameAgg);
        // 查询条件
        List<QueryCondition_ES> conditions = new ArrayList<>();
        // conditions.add(QueryCondition_ES.eq("op_type",opType));
        if(!"all".equals(type)){
            conditions.add(QueryCondition_ES.between("data_time",timeResult.get("startTime"),timeResult.get("endTime")));
            QueryBuilder queryBuilder = ElasticSearchUtil.toQueryBuilder(conditions);
            searchSourceBuilder.query(queryBuilder);
        }
        searchSourceBuilder.trackTotalHits(true);
        // 请求对象
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(indexName);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse =restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        Terms aggregation = searchResponse.getAggregations().get("groupName");
        List<? extends MultiBucketsAggregation.Bucket>  buckets = ((MultiBucketsAggregation) aggregation).getBuckets();
        Map<String,Object> data =new HashMap<>();
        for (MultiBucketsAggregation.Bucket bucket : buckets){
            String userName = bucket.getKeyAsString();
            ParsedSum sumCounts = bucket.getAggregations().get("opType");
            data.put(userName,sumCounts.getValue());
        }
        return  data;
    }


    /**
     * 根据type获取近一个月、近半年、 近一年
     * @param type
     * @param timeFormat
     * @return
     * @throws ParseException
     */
    private Map<String, String> getStartTimeAndEndTimeByTypeES(String type, String timeFormat) throws ParseException {
        Map<String, String> timeMap = new HashMap<>();
        String startTime = null;
        String endTime = null;
        Date curDate = new Date();
        switch (type) {
            case "month":  // 近一个月
                startTime = MettingCommonUtil.addMonth(curDate,timeFormat,-1) ;
                endTime =  MettingCommonUtil.addDay(curDate,timeFormat,1);
                break;
            case "halfyear": // 近半年
                startTime = MettingCommonUtil.addMonth(curDate,timeFormat,-6) ;
                endTime = MettingCommonUtil.addDay(curDate,timeFormat,1);
                break;
            case "year": // 近一年
                startTime = MettingCommonUtil.addNYear(curDate,timeFormat,-1) ;
                endTime = MettingCommonUtil.addDay(curDate,timeFormat,1);
                break;
            default:
                break;
        }
        timeMap.put("startTime",startTime);
        timeMap.put("endTime",endTime);
        return timeMap;
    }

    /**
     * 公文交换箱系统情况
     * type： month(近一个月)、halfyear(半年)、year(一年)
     * @return Result
     */
    @Override
    public ExchangeBoxVO exchangeBox(SituationLargeSearchVO searchVO) {
        ExchangeBoxVO data = new ExchangeBoxVO();
        data.setUserTotal(500);
        data.setUserLoginTotal(600);
        ExchangeBoxExtendVO vo = new ExchangeBoxExtendVO();
        vo.setRoamTotal(20);
        vo.setRegisterTotal(30);
        data.setReviceFile(vo);
        vo = new ExchangeBoxExtendVO();
        vo.setRoamTotal(50);
        vo.setRegisterTotal(10);
        data.setSignFile(vo);
        vo = new ExchangeBoxExtendVO();
        vo.setRoamTotal(90);
        vo.setRegisterTotal(80);
        data.setSecrecyFile(vo);
        return data;
    }
    /**
     * 本地区/跨地区文件交换占比
     *
     * @return Result
     */
    @Override
    public Map<String, Object> fileExchangePer(SituationLargeSearchVO searchVO) {
        Map<String, Object> data = new HashMap<>();
        // send_region,send_type分组统计
        List<Map<String, Object>> groupDatas = situationLargeScreenDao.getGroupBySendRegionAndSendType(searchVO.getType());
        int localNum = 0;
        int transRegional =0;
        for(Map<String, Object> map : groupDatas){
            String sendRegion = String.valueOf(map.get("sendRegion"));
            String sendType = String.valueOf(map.get("sendType"));
            int receiveNum = map.get("receiveNum") == null?0:Integer.parseInt(String.valueOf(map.get("receiveNum")));
            int sendNum = map.get("sendNum") == null?0:Integer.parseInt(String.valueOf(map.get("sendNum")));
            // 本地
            if("0".equals(sendRegion)){
                if("发件".equals(sendType)){
                    localNum = localNum + sendNum;
                }
                if("收件".equals(sendType)){
                    localNum = localNum + receiveNum;
                }
            }
            // 跨地区
            if("1".equals(sendRegion)){
                if("发件".equals(sendType)){
                    transRegional = transRegional + sendNum;
                }
                if("收件".equals(sendType)){
                    transRegional = transRegional + receiveNum;
                }
            }
        }
        data.put("local",localNum);
        data.put("transRegional",transRegional);
        return data;
    }
    /**
     * 地图
     * send_scope为全院
     * @return Result
     */
    @Override
    public List<String> branchMap(SituationLargeSearchVO searchVO) {
        return situationLargeScreenDao.branchMap(searchVO.getType());
    }

    /**
     * 根据city和时间范围查询
     * @param searchVO
     * @return
     */
    @Override
    public List<MapDetailVO> cityMapDetail(SituationLargeSearchVO searchVO) {
        Map<String,MapDetailVO> handle = new HashMap<>();
        String city = searchVO.getCity();
        String type = searchVO.getType();
        List<MapDetailQueryVO> groupDatas = situationLargeScreenDao.getGroupDeatailByCity(city,type);
        for(MapDetailQueryVO data : groupDatas){
            String orgName = data.getOrgName();
            String sendRegion =data.getSendRegion();
            String sendType = data.getSendType();
            int receiveNum = data.getReceiveNum();
            int sendNum = data.getSendNum();
            if(handle.containsKey(orgName)){
                MapDetailVO oldData = handle.get(orgName);
                addMapDetailVOData(oldData,sendRegion,sendType,receiveNum,sendNum);
                handle.put(orgName,oldData);
            }else{
                MapDetailVO vo = new MapDetailVO();
                addMapDetailVOData(vo,sendRegion,sendType,receiveNum,sendNum);
                vo.setName(orgName);
                handle.put(orgName,vo);
            }
        }
        Collection<MapDetailVO> collections = handle.values();
        List<MapDetailVO> resultDatas = collections.stream().collect(Collectors.toList());
        return resultDatas;
    }
    /**
     * 数据组装
     * sendRegion中1表示跨地区、0表示本地
     * sendType为发件、收件
     * @param data
     * @param sendRegion
     * @param sendType
     * @param receiveNum
     * @param sendNum
     */
    private void addMapDetailVOData(MapDetailVO data, String sendRegion,String sendType , int receiveNum, int sendNum) {
        // 本地
        if("0".equals(sendRegion)){
            if("发件".equals(sendType)){
                data.setLocalSendNum(sendNum);
            }
            if("收件".equals(sendType)){
                data.setLocalReceiveNum(receiveNum);
            }
        }
        // 跨地区
        if("1".equals(sendRegion)){
            if("发件".equals(sendType)){
                data.setTransRegionalSendNum(sendNum);
            }
            if("收件".equals(sendType)){
                data.setTransRegionalReceiveNum(receiveNum);
            }
        }
    }

}
