package com.vrv.vap.apicasom.business.task.service;

import com.vrv.vap.apicasom.business.task.bean.ZkySend;

import java.util.List;

/**
 * @author: 梁国露
 * @since: 2023/3/8 18:09
 * @description:
 */
public interface ZkySendDataService {


    public void excZkySend() throws InterruptedException;

    public void dataSyncHandle(String endTime, String startTime);


}
