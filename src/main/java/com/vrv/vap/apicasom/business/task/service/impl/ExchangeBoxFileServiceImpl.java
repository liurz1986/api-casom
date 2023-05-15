package com.vrv.vap.apicasom.business.task.service.impl;

import com.vrv.vap.apicasom.business.task.bean.ExchangeBoxFile;
import com.vrv.vap.apicasom.business.task.bean.ZkySend;
import com.vrv.vap.apicasom.business.task.service.ExchangeBoxFileService;
import com.vrv.vap.apicasom.business.task.service.ZkySendService;
import com.vrv.vap.apicasom.business.task.service.repository.ExchangeBoxFileRespository;
import com.vrv.vap.apicasom.business.task.service.repository.ZkySendRespository;
import com.vrv.vap.jpa.basedao.BaseRepository;
import com.vrv.vap.jpa.baseservice.impl.BaseServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: 梁国露
 * @since: 2023/2/17 09:46
 * @description:
 */
@Service
public class ExchangeBoxFileServiceImpl extends BaseServiceImpl<ExchangeBoxFile, String> implements ExchangeBoxFileService {
    @Autowired
    private ExchangeBoxFileRespository exchangeBoxFileRespository;

    @Override
    public BaseRepository<ExchangeBoxFile, String> getRepository() {
        return exchangeBoxFileRespository;
    }


}
