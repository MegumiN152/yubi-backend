package com.yupi.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.Scanner;

public class TopicProducer {
    // 定义交换机名称
    private static final String EXCHANGE_NAME = "topic_exchange";

    public static void main(String[] argv) throws Exception {
        // 创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        // 设置连接工厂的主机地址为本地主机
        factory.setHost("localhost");
        // 建立连接并创建通道
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            // 使用通道声明交换机，类型为direct
            channel.exchangeDeclare(EXCHANGE_NAME, "topic");
            // 获取严重程度（路由键）和消息内容
            Scanner scanner = new Scanner(System.in);
            while(scanner.hasNext()){
                String userInput = scanner.nextLine();
                String[] split = userInput.split(" ");
                if (split.length<1){
                    continue;
                }
                String message=split[0];
                String routingKey=split[1];
                // 发布消息到交换机
                channel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes("UTF-8"));
                System.out.println(" [x] Sent '" + message + "' with routing:'" + routingKey + "'");
            }
        }
    }
    //..
}
