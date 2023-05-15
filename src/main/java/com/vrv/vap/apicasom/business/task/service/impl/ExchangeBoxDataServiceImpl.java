package com.vrv.vap.apicasom.business.task.service.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vrv.vap.apicasom.business.task.bean.ExchangeBoxFile;
import com.vrv.vap.apicasom.business.task.bean.ExchangeBoxUsr;
import com.vrv.vap.apicasom.business.task.bean.httpres.exchange.ExchangeFileRes;
import com.vrv.vap.apicasom.business.task.bean.httpres.exchange.ExchangeUsrRes;
import com.vrv.vap.apicasom.business.task.service.ExchangeBoxDataService;
import com.vrv.vap.apicasom.business.task.service.ExchangeBoxFileService;
import com.vrv.vap.apicasom.business.task.service.ExchangeBoxUsrService;
import com.vrv.vap.apicasom.frameworks.util.HttpClientUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: 梁国露
 * @since: 2023/5/10 14:38
 * @description:
 */
@Service
public class ExchangeBoxDataServiceImpl implements ExchangeBoxDataService {

    /**
     * gson对象
     */
    private static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    @Autowired
    private ExchangeBoxFileService exchangeBoxFileService;

    @Autowired
    private ExchangeBoxUsrService exchangeBoxUsrService;

    @Override
    public ExchangeBoxFile getExchangeBoxFiles(String url, String time,String fileType) {
        Map<String,Object> param = new HashMap<>();
        time = time.replace(" ","%");
        param.put("endTime",time);
        param.put("fileType",fileType);

        Map<String,String> header = new HashMap<>();
        header.put("Content-type","application/json;charset=UTF-8");
        String res = HttpClientUtils.doGet(url,param,header);
        if(StringUtils.isNotBlank(res)){
            ExchangeFileRes fileRes = gson.fromJson(res,ExchangeFileRes.class);
            ExchangeBoxFile file = fileRes.getData();
            return file;
        }
        return null;
    }

    @Override
    public ExchangeBoxUsr getExchangeBoxUsrs(String url, String time) {
        Map<String,Object> param = new HashMap<>();
        time = time.replace(" ","%");
        param.put("endTime",time);
        Map<String,String> header = new HashMap<>();
        header.put("Content-type","application/json;charset=UTF-8");
        String res = HttpClientUtils.doGet(url,param,header);
        if(StringUtils.isNotBlank(res)){
            ExchangeUsrRes usrRes = gson.fromJson(res,ExchangeUsrRes.class);
            ExchangeBoxUsr usr = usrRes.getData();
            return usr;
        }
        return null;
    }

    @Override
    public void saveExchangeBoxFile(ExchangeBoxFile file) {
        exchangeBoxFileService.save(file);
    }

    @Override
    public void saveExchageBoxUsr(ExchangeBoxUsr usr) {
        exchangeBoxUsrService.save(usr);
    }
}
