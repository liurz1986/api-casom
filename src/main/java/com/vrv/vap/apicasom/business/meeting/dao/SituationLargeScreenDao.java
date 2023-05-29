package com.vrv.vap.apicasom.business.meeting.dao;

import com.vrv.vap.apicasom.business.meeting.vo.AbnormalMettingTrendVO;
import com.vrv.vap.apicasom.business.meeting.vo.KeyValueQueryVO;
import com.vrv.vap.apicasom.business.meeting.vo.SituationLargeSearchVO;
import com.vrv.vap.apicasom.business.meeting.vo.TreandVO;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface SituationLargeScreenDao {
    /**
     * 公文及文件交换系统发件数量、收件数量top10
     * @param searchType
     * @param timeType
     * @return
     */
  public List<KeyValueQueryVO> fileSendAndReceiveNumTop10(String searchType,String timeType);

    /**
     * 发件和收件情况统计
     * filterType: 1: 本地收件 2：本地发件 3：跨地区收件 4：跨地区发件
     * @param xStrength
     * @param filterType
     * @param type
     * @return
     */
  public List<TreandVO> getFileSendAndReceiveTreandStatistics(String xStrength, String filterType, String type);

    /**
     * 获取zky_send表中最大、最小时间
     * @return
     */
  public Map<String, Object> getMaxAndMinStartTime();

  /**
   * 院机关各部门邮件收发数量
   * @param s
   * @param type
   * @return
   */
  public  List<KeyValueQueryVO> emailSendAndReceiveNum(String s, String type);

  /**
   * 统计总数
   * @param type
   * @return
   */
  public Map<String, Object> emailSendAndReceiveTotal(String type);
}
