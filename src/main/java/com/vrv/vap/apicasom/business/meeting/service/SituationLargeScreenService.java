package com.vrv.vap.apicasom.business.meeting.service;

import com.vrv.vap.apicasom.business.meeting.vo.*;

import java.util.List;
import java.util.Map;

public interface SituationLargeScreenService {
    public FileSendAndReceiveNumVO fileSendAndReceiveNum(SituationLargeSearchVO searchVO);

    public List<FileSendAndReceiveVO> fileSendAndReceiveTrend(SituationLargeSearchVO searchVO);

    public EmailSendAndReceiveNumVO emailSendAndReceiveNum(SituationLargeSearchVO searchVO);

    public List<FileSendAndReceiveVO> fileSendAndReceiveTab(SituationLargeSearchVO searchVO, String tabName);

    public List<PrintingAndBurningNumVO> printingAndBurningNum(SituationLargeSearchVO searchVO);

    public ExchangeBoxVO exchangeBox(SituationLargeSearchVO searchVO);

    public Map<String,Object> fileExchangePer(SituationLargeSearchVO searchVO);

    public List<String> branchMap(SituationLargeSearchVO searchVO);

    public List<MapDetailVO> cityMapDetail(SituationLargeSearchVO searchVO);
}
