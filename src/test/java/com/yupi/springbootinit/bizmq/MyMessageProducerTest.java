package com.yupi.springbootinit.bizmq;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author 黄昊
 * @version 1.0
 **/
@SpringBootTest
class MyMessageProducerTest {
    @Resource
    private BiMessageProducer biMessageProducer;

    @Test
    void sendMessage() {

    }
}