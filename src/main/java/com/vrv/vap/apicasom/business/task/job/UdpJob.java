package com.vrv.vap.apicasom.business.task.job;

import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * udp测试用
 */
@Component
public class UdpJob implements CommandLineRunner {
    Logger looger = LoggerFactory.getLogger(UdpJob.class);
    @Override
    public void run(String... args) throws Exception {
        new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                looger.error("==============udp测试开启===================");
                // 创建DatagramSocket对象，指定监听端口号
                DatagramSocket socket = new DatagramSocket(20519);
                // 创建DatagramPacket对象，用于接收数据
                byte[] buf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                // 接收数据
                while(true){
                    socket.receive(packet);
                    // 输出接收到的数据
                    String data = new String(packet.getData(), 0, packet.getLength());
                    looger.error("接收到的数据：" + data);
                }
            }
        }).start();

    }
}
