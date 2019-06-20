package com.xiaochen.rabbitmq.adapter;



import com.xiaochen.rabbitmq.entity.Order;
import com.xiaochen.rabbitmq.entity.Packaged;

import java.io.File;
import java.util.Map;

/**
 * @author xiaochen
 * @create 2019-06-05 14:37
 */
public class MessageDelegate {
    public void handleMessage(byte[] messageBody) {
        System.out.println("默认方法, 消息内容:" + new String(messageBody));
    }

    public void consumeMessage(byte[] messageBody) {
        System.out.println("字节数组方法, 消息内容:" + new String(messageBody));
    }

    public void consumeMessage(String messageBody) {
        System.out.println("字符串方法, 消息内容:" + messageBody);
    }

   public void method1(String messageBody) {
        System.out.println("method1 收到消息内容:" + new String(messageBody));
    }

    public void method2(String messageBody) {
        System.out.println("method2 收到消息内容:" + new String(messageBody));
    }

    public void consumeMessage(Map messageBody) {
        System.out.println("map方法, 消息内容:" + messageBody);
    }

    public void consumeMessage(Order order) {
        System.out.println("order对象, 消息内容, id: " + order.getId() +
                ", name: " + order.getName() +
                ", content: "+ order.getContent());
    }

    public void consumeMessage(Packaged pack) {
        System.out.println("package对象, 消息内容, id: " + pack.getId() +
                ", name: " + pack.getName() +
                ", content: "+ pack.getDescription());
    }

    public void consumeMessage(File file) {
        System.out.println("文件对象 方法, 消息内容:" + file.getName());
    }
}
