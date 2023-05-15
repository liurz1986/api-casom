package com.vrv.vap.apicasom.business.task.job;

import com.vrv.vap.apicasom.business.task.bean.ExchangeBoxFile;
import com.vrv.vap.apicasom.business.task.bean.ExchangeBoxUsr;
import com.vrv.vap.apicasom.business.task.constant.MeetingUrlConstant;
import com.vrv.vap.apicasom.business.task.service.ExchangeBoxDataService;
import com.vrv.vap.jpa.common.DateUtil;
import com.vrv.vap.jpa.common.UUIDUtils;
import org.checkerframework.checker.units.qual.A;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author: 梁国露
 * @since: 2023/5/10 14:19
 * @description:
 */
@Component
@EnableScheduling
public class ExchangeBoxJob {
    // 日志
    private Logger logger = LoggerFactory.getLogger(ExchangeBoxJob.class);

    @Autowired
    private ExchangeBoxDataService exchangeBoxDataService;

    @Value("${hw.exchange-box.url}")
    private String url;

    @Scheduled(cron = "${hw.exchange-box.time}")
    public void getExchangeBox(){
        Date date = new Date();
        String time = DateUtil.format(date,DateUtil.DEFAULT_DATE_PATTERN);
        // 获取文件信息
        String fileUrl = url + MeetingUrlConstant.EXCHANGE_FILES_URL;
        String fileType = "1";
        ExchangeBoxFile file =exchangeBoxDataService.getExchangeBoxFiles(fileUrl,time,fileType);

        // 保存文件信息
        if(file != null){
            String guid = UUIDUtils.get32UUID();
            file.setFileType(fileType);
            file.setCreateTime(date);
            file.setGuid(guid);
            exchangeBoxDataService.saveExchangeBoxFile(file);
        }
        // 获取用户信息
        String usrUrl = url + MeetingUrlConstant.EXCHANGE_USRS_URL;
        ExchangeBoxUsr usr = exchangeBoxDataService.getExchangeBoxUsrs(usrUrl,time);

        // 保存用户信息
        if(usr != null){
            String guid = UUIDUtils.get32UUID();
            usr.setCreateTime(date);
            usr.setGuid(guid);
            exchangeBoxDataService.saveExchageBoxUsr(usr);
        }
    }
}
