package com.vrv.vap.apicasom.business.meeting.service.impl;

import com.vrv.vap.apicasom.business.meeting.service.SituationLargeScreenService;
import com.vrv.vap.apicasom.business.meeting.vo.*;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 态势应用大屏
 *
 * 2023-5-27
 */

@Service
public class SituationLargeScreenServiceImpl implements SituationLargeScreenService {
    /**
     * 公文及文件交换系统发件数量、收件数量
     * @param searchVO
     * @return
     */
    @Override
    public FileSendAndReceiveNumVO fileSendAndReceiveNum(SituationLargeSearchVO searchVO) {
        FileSendAndReceiveNumVO result = new FileSendAndReceiveNumVO();
        List<KeyValueQueryVO> send = new ArrayList<>();
        KeyValueQueryVO vo = new KeyValueQueryVO();
        vo.setKey("院部机关");
        vo.setValue("20");
        send.add(vo);
        vo = new KeyValueQueryVO();
        vo.setKey("上海天文台");
        vo.setValue("18");
        send.add(vo);
        List<KeyValueQueryVO> receive = new ArrayList<>();
        vo = new KeyValueQueryVO();
        vo.setKey("院部机关");
        vo.setValue("26");
        receive.add(vo);
        vo = new KeyValueQueryVO();
        vo.setKey("广州分院");
        vo.setValue("24");
        receive.add(vo);
        result.setReceive(receive);
        result.setSend(send);
        return result;
    }

    /**
     * 发件和收件情况统计
     * 1.本地区收件
     * 2.本地地区发件
     * 3.跨地区收件
     * 4.跨地区发件
     * @param searchVO
     * @return
     */
    @Override
    public List<FileSendAndReceiveVO> fileSendAndReceiveTrend(SituationLargeSearchVO searchVO) {
        List<FileSendAndReceiveVO> result = new ArrayList<>();
        FileSendAndReceiveVO data = new FileSendAndReceiveVO();
        data.setLocalSendNum(10);
        data.setLocalReceiveNum(20);
        data.setTransRegionalReceiveNum(14);
        data.setTransRegionalSendNum(48);
        data.setName("2023/5/1");
        result.add(data);
        data = new FileSendAndReceiveVO();
        data.setLocalSendNum(100);
        data.setLocalReceiveNum(210);
        data.setTransRegionalReceiveNum(140);
        data.setTransRegionalSendNum(484);
        data.setName("2023/5/2");
        result.add(data);
        data = new FileSendAndReceiveVO();
        data.setLocalSendNum(235);
        data.setLocalReceiveNum(145);
        data.setTransRegionalReceiveNum(89);
        data.setTransRegionalSendNum(45);
        data.setName("2023/5/3");
        result.add(data);
        return result;
    }
    /**
     * 院机关各部门邮件收发数量
     * @return Result
     */
    @Override
    public EmailSendAndReceiveNumVO emailSendAndReceiveNum(SituationLargeSearchVO searchVO) {
        EmailSendAndReceiveNumVO result = new EmailSendAndReceiveNumVO();
        List<KeyValueQueryVO> sendList = new ArrayList<>();
        KeyValueQueryVO vo = new KeyValueQueryVO();
        vo.setKey("办公厅");
        vo.setValue("20");
        sendList.add(vo);
        vo.setKey("重任局");
        vo.setValue("18");
        sendList.add(vo);
        vo.setKey("离退局");
        vo.setValue("14");
        sendList.add(vo);

        List<KeyValueQueryVO>  recList = new ArrayList<>();
        vo = new KeyValueQueryVO();
        vo.setKey("办公厅");
        vo.setValue("26");
        recList.add(vo);
        vo.setKey("重任局");
        vo.setValue("19");
        recList.add(vo);
        vo.setKey("离退局");
        vo.setValue("14");
        recList.add(vo);

        result.setReceiveTotal(500);
        result.setSendTotal(600);
        result.setSendTop(sendList);
        result.setReceiveTop(recList);
        return result;
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
        FileSendAndReceiveVO data = new FileSendAndReceiveVO();
        data.setLocalSendNum(10);
        data.setLocalReceiveNum(20);
        data.setTransRegionalReceiveNum(14);
        data.setTransRegionalSendNum(48);
        data.setName("北京地区");
        result.add(data);
        return result;
    }
    /**
     * 打印和刻录数量
     * @return Result
     */
    @Override
    public List<PrintingAndBurningNumVO> printingAndBurningNum(SituationLargeSearchVO searchVO) {
        List<PrintingAndBurningNumVO> result = new ArrayList<>();
        PrintingAndBurningNumVO data = new PrintingAndBurningNumVO();
        data.setPrintingNum(10);
        data.setBurningNum(14);
        result.add(data);
        return result;
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
        data.put("local",80);
        data.put("transRegional",20);
        return data;
    }
    /**
     * 地图
     *
     * @return Result
     */
    @Override
    public List<String> branchMap(SituationLargeSearchVO searchVO) {
        List<String> citys = new ArrayList<>();
        citys.add("武汉");
        citys.add("上海");
        return citys;
    }

    @Override
    public List<MapDetailVO> cityMapDetail(SituationLargeSearchVO searchVO) {
        List<MapDetailVO> result = new ArrayList<>();
        MapDetailVO vo = new MapDetailVO();
        vo.setName("力学研究所");
        vo.setLocalSendNum(12);
        vo.setLocalReceiveNum(25);
        vo.setTransRegionalReceiveNum(45);
        vo.setTransRegionalSendNum(18);
        result.add(vo);
        return result;
    }
}
