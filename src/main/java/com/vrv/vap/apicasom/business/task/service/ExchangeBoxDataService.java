package com.vrv.vap.apicasom.business.task.service;

import com.vrv.vap.apicasom.business.task.bean.ExchangeBoxFile;
import com.vrv.vap.apicasom.business.task.bean.ExchangeBoxUsr;

import java.util.List;
import java.util.Map;

/**
 * @author: 梁国露
 * @since: 2023/5/10 14:37
 * @description:
 */
public interface ExchangeBoxDataService {
    public ExchangeBoxFile getExchangeBoxFiles(String url, String time,String fileType);

    public ExchangeBoxUsr getExchangeBoxUsrs(String url, String time);

    public void saveExchangeBoxFile(ExchangeBoxFile file);

    public void saveExchageBoxUsr(ExchangeBoxUsr usr);
}
