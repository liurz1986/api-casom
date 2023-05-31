package com.vrv.vap.apicasom.business.meeting.service;

import com.vrv.vap.apicasom.business.meeting.vo.*;
import com.vrv.vap.jpa.web.Result;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

public interface SituationLargeScreenService {
    public FileSendAndReceiveNumVO fileSendAndReceiveNum(SituationLargeSearchVO searchVO);

    public List<FileSendAndReceiveVO> fileSendAndReceiveTrend(SituationLargeSearchVO searchVO) throws ParseException;

    public EmailSendAndReceiveNumVO emailSendAndReceiveNum(SituationLargeSearchVO searchVO);

    public List<FileSendAndReceiveVO> fileSendAndReceiveTab(SituationLargeSearchVO searchVO, String tabName);

    public Result<List<PrintingAndBurningNumVO>> printingAndBurningNum(SituationLargeSearchVO searchVO) throws ParseException, IOException;

    public ExchangeBoxVO exchangeBox(SituationLargeSearchVO searchVO) throws ParseException;

    public Map<String,Object> fileExchangePer(SituationLargeSearchVO searchVO);

    public List<String> branchMap(SituationLargeSearchVO searchVO);

    public List<MapDetailVO> cityMapDetail(SituationLargeSearchVO searchVO);
}
