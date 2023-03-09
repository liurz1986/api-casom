package com.vrv.vap.apicasom.business.task.service.impl;

import com.vrv.vap.apicasom.business.task.bean.ZkySend;
import com.vrv.vap.apicasom.business.task.bean.ZkyUnitBean;
import com.vrv.vap.apicasom.business.task.service.ZkySendService;
import com.vrv.vap.apicasom.business.task.service.ZkyUnitService;
import com.vrv.vap.apicasom.business.task.service.repository.ZkySendRespository;
import com.vrv.vap.apicasom.business.task.service.repository.ZkyUnitRespository;
import com.vrv.vap.jpa.basedao.BaseRepository;
import com.vrv.vap.jpa.baseservice.impl.BaseServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author: 梁国露
 * @since: 2023/2/17 09:46
 * @description:
 */
@Service
public class ZkySendServiceImpl extends BaseServiceImpl<ZkySend, String> implements ZkySendService {
    @Autowired
    private ZkySendRespository zkySendRespository;

    @Override
    public BaseRepository<ZkySend, String> getRepository() {
        return zkySendRespository;
    }


}
