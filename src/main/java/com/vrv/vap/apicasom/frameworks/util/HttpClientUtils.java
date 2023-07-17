package com.vrv.vap.apicasom.frameworks.util;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: 梁国露
 * @since: 2022/12/30 16:17
 * @description:
 */
public class HttpClientUtils {

    private static Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);

    /**
     * gson对象
     */
    private static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    public static String doGet(String url, Map<String, Object> param,Map<String,String> header){
        logger.info("GET请求：URL，"+url);
        logger.info("GET请求：请求参数，"+ (param==null?null:JSON.toJSONString(param)));
        logger.info("GET请求：请求头，"+(header==null?null:JSON.toJSONString(header)));
        // 创建Httpclient对象
        HttpClient httpclient =wrapClient();

        String resultString = "";
        HttpResponse response = null;
        try {
            // 创建uri
//            URIBuilder builder = new URIBuilder(url);
//            if (param != null) {
//                for (String key : param.keySet()) {
//                    builder.addParameter(key, param.get(key));
//                }
//            }
//            URI uri = builder.build();
            if(param!=null){
                String paramStr = "?";
                List<String> paramList = new ArrayList<>();
                for(Map.Entry<String,Object> entry: param.entrySet()){
                    paramList.add(entry.getKey()+"="+entry.getValue());
                }
                if(!paramList.isEmpty()){
                    paramStr += String.join("&",paramList);
                    url = url+paramStr;
                }
            }
            // 创建http GET请求
            HttpGet httpGet = new HttpGet(url);
            if(header != null){
                for(Map.Entry<String,String> entry:header.entrySet()){
                    httpGet.addHeader(entry.getKey(), entry.getValue());
                }
            }
            // 执行请求
            response = httpclient.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            // 判断返回状态是否为200
            if (statusCode == 200) {
                resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
            }else{
                logger.error("请求接口失败,接口返回statusCode:"+statusCode);
                HttpEntity entity =  response.getEntity();
                logger.error("请求接口失败，接口返回entity:"+(entity == null?null: JSON.toJSONString(entity)));
                throw new RuntimeException("请求接口失败,接口返回statusCode:"+statusCode);
            }
        } catch (Exception e) {
            logger.error("{}请求失败，msg={}",url,e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
        return resultString;
    }

    public static String doGet(String url, Map<String, Object> param) {
        return doGet(url,param,null);
    }

    public static String doGet(String url) {
        return doGet(url, null);
    }

    public static String doPost(String url, Map<String, Object> param,Map<String,String> header){
        logger.info("post请求：URL，"+url);
        logger.info("post请求：请求参数，"+ (param==null?null:JSON.toJSONString(param)));
        logger.info("post请求：请求头，"+(header==null?null:JSON.toJSONString(header)));
        // 创建Httpclient对象
        HttpClient httpClient = wrapClient();
        HttpResponse response = null;
        String resultString = "";
        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);
            if(header != null){
                for(Map.Entry<String,String> entry:header.entrySet()){
                    httpPost.addHeader(entry.getKey(), entry.getValue());
                }
            }
            // 创建参数列表
            if (param != null) {
//                List<NameValuePair> paramList = new ArrayList<>();
//                for (String key : param.keySet()) {
//                    paramList.add(new BasicNameValuePair(key, param.get(key)));
//                }
//                // 模拟表单
//                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramList,"utf-8");
//                httpPost.setEntity(entity);
                // 模拟表单
                String body = gson.toJson(param);
                StringEntity entity = new StringEntity(body,"UTF-8");
                httpPost.setEntity(entity);
            }
            // 执行http请求
            response = httpClient.execute(httpPost);
           int statusCode = response.getStatusLine().getStatusCode();
            // 判断返回状态是否为200
            if (statusCode == 200) {
                resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
            }else{
                logger.error("请求接口失败,接口返回statusCode:"+statusCode);
                HttpEntity entity =  response.getEntity();
                logger.error("请求接口失败，接口返回entity:"+(entity == null?null: JSON.toJSONString(entity)));
                throw new RuntimeException("请求接口失败,接口返回statusCode:"+statusCode);
            }
        } catch (Exception e) {
            logger.error("{}请求失败，msg={}",url,e.getLocalizedMessage());
            throw new RuntimeException(e);
        }

        return resultString;
    }

    public static String doPost(String url, Map<String, Object> param) {
        return doPost(url,param,null);
    }

    public static String doPost(String url) {
        return doPost(url, null);
    }

    public static String doPostJson(String url, String json) {
        // 创建Httpclient对象
        HttpClient httpClient = wrapClient();
        HttpResponse response = null;
        String resultString = "";
        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);
            // 创建请求内容
            StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
            httpPost.setEntity(entity);
            // 执行http请求
            response = httpClient.execute(httpPost);
            // 判断返回状态是否为200
            if (response.getStatusLine().getStatusCode() == 200) {
                resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
            }else{
                logger.error("{}请求失败！接口返回：{}",url,response.getEntity().toString());
            }
        } catch (Exception e) {
            logger.error("{}请求失败，msg={}",url,e.getLocalizedMessage());
            throw new RuntimeException(e);
        }

        return resultString;
    }

    public static HttpClient wrapClient() {
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            X509TrustManager tm = new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] arg0,
                                               String arg1) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] arg0,
                                               String arg1) throws CertificateException {
                }
            };
            ctx.init(null, new TrustManager[] { tm }, null);
            SSLConnectionSocketFactory ssf = new SSLConnectionSocketFactory(
                    ctx, NoopHostnameVerifier.INSTANCE);
            CloseableHttpClient httpclient = HttpClients.custom()
                    .setSSLSocketFactory(ssf).build();
            return httpclient;
        } catch (Exception e) {
            return HttpClients.createDefault();
        }
    }
}
