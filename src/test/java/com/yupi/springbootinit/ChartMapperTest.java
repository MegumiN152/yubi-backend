package com.yupi.springbootinit;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.yupi.springbootinit.mapper.ChartMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ChartMapperTest {

    @Resource
    private ChartMapper chartMapper;

        public static void main(String[] args) throws Exception {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("47.120.1.180");
            factory.setPort(5672);
            factory.setUsername("admin");
            factory.setPassword("abcdef");
            factory.setVirtualHost("/");
            try (Connection connection = factory.newConnection()) {
                System.out.println("Connected to RabbitMQ successfully!");
            }
        }



}