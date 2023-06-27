package com.vrv.vap.apicasom.business.task.dao;

public interface ZkySendDao {
    public boolean isRepeat(String time);

    public void deleteRepeatTimeData(String dateTime);
}
