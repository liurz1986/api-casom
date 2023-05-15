package com.vrv.vap.apicasom.business.task.service.impl;

import com.vrv.vap.apicasom.business.task.bean.ExchangeBoxFile;
import com.vrv.vap.apicasom.business.task.bean.ExchangeBoxUsr;
import com.vrv.vap.apicasom.business.task.service.ExchangeBoxFileService;
import com.vrv.vap.apicasom.business.task.service.ExchangeBoxUsrService;
import com.vrv.vap.apicasom.business.task.service.repository.ExchangeBoxFileRespository;
import com.vrv.vap.apicasom.business.task.service.repository.ExchangeBoxUsrRespository;
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
public class ExchangeBoxUsrServiceImpl extends BaseServiceImpl<ExchangeBoxUsr, String> implements ExchangeBoxUsrService {
    @Autowired
    private ExchangeBoxUsrRespository exchangeBoxUsrRespository;

    @Override
    public BaseRepository<ExchangeBoxUsr, String> getRepository() {
        return exchangeBoxUsrRespository;
    }


}
