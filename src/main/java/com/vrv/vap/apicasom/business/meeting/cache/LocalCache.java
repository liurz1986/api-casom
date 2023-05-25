package com.vrv.vap.apicasom.business.meeting.cache;

import com.vrv.vap.apicasom.business.meeting.bean.ZkyPrintUserOrg;

import java.util.ArrayList;
import java.util.List;

public class LocalCache {

    // 用户打印机构数据缓存数据
    private static List<ZkyPrintUserOrg> zkyPrintUserOrgCaches= new ArrayList<>();

    // 全量更新缓存
    public static void upDateAllZkyPrintUserOrgCache(List<ZkyPrintUserOrg> list){
        zkyPrintUserOrgCaches.clear();
        zkyPrintUserOrgCaches.addAll(list);
    }
    // 查询缓存
    public static List<ZkyPrintUserOrg> getAllZkyPrintUserOrgCache(){
        return zkyPrintUserOrgCaches;
    }
    // 清空缓存
    public static void clearAll(){
        zkyPrintUserOrgCaches.clear();
    }
}
