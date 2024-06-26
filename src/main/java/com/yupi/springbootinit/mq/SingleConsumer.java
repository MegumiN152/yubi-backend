package com.yupi.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * @author 黄昊
 * @version 1.0
 **/
public class SingleConsumer {
    private final static String QUEUE_NAME="hello";

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME,false,false,false,null);
        System.out.println("[*]wait for messages.To exit press CTRL+C");
        DeliverCallback deliverCallback=(consumerTag,delivery)->{
            String s = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println("[x] Received+"+s);
        };
        channel.basicConsume(QUEUE_NAME,true,deliverCallback,consumerTag->{});
    }
}
