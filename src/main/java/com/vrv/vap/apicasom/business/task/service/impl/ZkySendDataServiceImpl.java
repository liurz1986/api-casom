package com.vrv.vap.apicasom.business.task.service.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.vrv.vap.apicasom.business.task.bean.ZkySend;
import com.vrv.vap.apicasom.business.task.bean.httpres.send.SendResp;
import com.vrv.vap.apicasom.business.task.service.ZkySendDataService;
import com.vrv.vap.apicasom.frameworks.util.HttpClientUtils;
import com.vrv.vap.jpa.common.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.*;

/**
 * @author: 梁国露
 * @since: 2023/3/8 18:09
 * @description:
 */
@Service
public class ZkySendDataServiceImpl implements ZkySendDataService {

    // 日志
    private Logger logger = LoggerFactory.getLogger(MeetingHttpServiceImpl.class);

    /**
     * gson对象
     */
    private static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    @Value("${hw.send.url}")
    private String zkySendUrl;

    @Override
    public List<ZkySend> getZkySend(String startTime,String endTime,String type) {
        List<ZkySend> zkySends = new ArrayList<>();
        Map<String,Object> param = new HashMap<>();
        param.put("startTime",startTime);
        param.put("endTime",endTime);
        param.put("sendType",type);
        param.put("sendScope","院部机关");
        Map<String,String> header = new HashMap<>();
        header.put("Content-type","application/json;charset=UTF-8");
        String res = HttpClientUtils.doPost(zkySendUrl,param,header);

        if(StringUtils.isNotBlank(res)){
            Type typeToken = new TypeToken<List<ZkySend>>(){}.getType();
            List<ZkySend> zkySendList =gson.fromJson(res,typeToken);
            zkySends.addAll(zkySendList);
        }
        return zkySends;
    }
}
