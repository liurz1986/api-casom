package com.vrv.vap.apicasom.frameworks.util;


import com.vrv.vap.apicasom.business.task.bean.MeetingQueueVo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.SynchronousQueue;

/**
 * @author 梁国露
 * @date 2021年11月10日 10:10
 */
public class QueueUtil {
    /**
     * 初始化有界队列队列
     */
    private static final ConcurrentLinkedQueue<MeetingQueueVo> LINKED_BLOCKING_QUEUE = new ConcurrentLinkedQueue<MeetingQueueVo>();

    public static void put(MeetingQueueVo meetingSyncVo) {
            boolean flag = LINKED_BLOCKING_QUEUE.add(meetingSyncVo);
    }

    public static MeetingQueueVo poll() {
        MeetingQueueVo meetingSyncVo = LINKED_BLOCKING_QUEUE.poll();
        return meetingSyncVo;
    }
}
