package com.vrv.vap.apicasom.frameworks.util;

import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 读取yml文件数据
 */
public class YmlUtil {
	 /**
     * key:文件名索引
     * value:配置文件内容
     */
    private static Map<String, LinkedHashMap> ymls = new HashMap<>();
    
    
    /**
     * string:当前线程需要查询的文件名
     */
    private static ThreadLocal<String> nowFileName = new ThreadLocal<>();

    /**
     * 加载配置文件
     * @param fileName
     */
    public static void loadYml(String fileName) {
        nowFileName.set(fileName);
        if (!ymls.containsKey(fileName)) {
            ymls.put(fileName, new Yaml().loadAs(YmlUtil.class.getResourceAsStream("/" + fileName), LinkedHashMap.class));
        }
    }

    /**
     * 获得Yml文件对应的内容，以Map格式
     * @param fileName
     * @return
     */
    public static Map getYmlMap(String fileName){
    	loadYml(fileName);
    	Map ymlInfo = (Map) ymls.get(nowFileName.get()).clone();
    	return ymlInfo;
    }
    
    
    public static Object getValue(String key) throws Exception {
        // 将配置文件进行复制
        Map ymlInfo = (Map) ymls.get(nowFileName.get()).clone();
        Object object = ymlInfo.get(key);
        if(object!=null) {
        	return object;
        }else {
            throw new RuntimeException("读取yaml信息异常！");
        }

    }

    /**
     * 解析对应的port
     * @param fileName
     * @param key
     * @return
     */
    public static Object getValue(String fileName, String key) {
        // 首先加载配置文件
    	String getenv = System.getenv(key);   //读取环境变量
    	if(StringUtils.isNotEmpty(getenv)){  //读取系统环境变量
    		String url = getenv;
    		return  url;
    	}else{  //读取文件变量
    		try {
    			loadYml(fileName);
    			return getValue(key);
    		}catch(Exception e) {
    			throw new RuntimeException("解析失败", e);
    		}    		
    	}
    	
    	
    }

    public static void main(String[] args) throws Exception {
//    	StringBuilder stringBuilder=new StringBuilder();
//    	Map<String,Object> ymlMap = getYmlMap("assets.yml");
//    	for (Map.Entry<String,Object> entry : ymlMap.entrySet()) { 
//    		  String key = entry.getKey();
//    		  Map<String,Object> value = (Map)entry.getValue();
//    		  String assetType = value.get("type").toString();
////   		  assetType = assetType.substring(assetType.indexOf("[")+1, assetType.lastIndexOf("]"));
////    		  System.out.println(key);
////    		  System.out.println(assetType);
//    		  stringBuilder.append(key).append(": ").append("\n\t").append("type").append(": ").append(assetType).append("\n");
//    		}
//    	String string = stringBuilder.toString();
//        System.out.println(string);
    	 String string = YmlUtil.getValue("application.yml", "topic_producer_name").toString();
    	 System.out.println(string);
    	
    }
    
}
