package com.vrv.vap.apicasom.frameworks.util;


import java.util.Map;
import java.util.concurrent.SynchronousQueue;

/**
 * @author 梁国露
 * @date 2021年11月10日 10:10
 */
public class QueueUtil {
    /**
     * 初始化有界队列队列
     */
    private static final SynchronousQueue<Map<String,String>> LINKED_BLOCKING_QUEUE = new SynchronousQueue<Map<String,String>>();

    public static void put(Map<String,String> warnResultLogTmpVO) {
        try {
            LINKED_BLOCKING_QUEUE.put(warnResultLogTmpVO);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static Map<String,String> poll() {
        Map<String,String> warnResultLogTmpVO = LINKED_BLOCKING_QUEUE.poll();
        return warnResultLogTmpVO;
    }
}
