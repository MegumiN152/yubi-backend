package com.yupi.springbootinit.utils;




import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author 黄昊
 * @version 1.0
 **/
@SpringBootTest
public class OpenAiTest {
//    @Test
//    public void openaitest() {
//        ChatGPTClient client = ChatGPTClient.builder()
//                .apiKey("sk-c78ecf4406e14b7380ef66032940a7ef")
//                .requestTimeout(60000L) // optional, default is 60000 ms
//                .baseUrl("https://api.deepseek.com") // optional
//                .build();
//        ChatRequest request = new ChatRequest("Write an essay about AI revolution");
//        request.setModel("deepseek-chat");
//        ChatResponse chatResponse = client.sendChat(request);
//        System.out.println(chatResponse.getChoices().get(0).getMessage().getContent());
//    }
    @Test
    public void openStreamTest(){

    }

}
