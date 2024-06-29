package com.yupi.springbootinit.utils;

import com.hh.deepseeksdk.client.DsApiClient;
import com.hh.deepseeksdk.model.ChatRequest;
import com.hh.deepseeksdk.model.DeepseekChatCompletionResponse;
import com.yupi.springbootinit.constant.CommonConstant;

import com.yupi.springbootinit.manager.AiManager;
import com.yupi.springbootinit.model.dto.chart.ChartGenResult;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author 黄昊
 * @version 1.0
 **/
@SpringBootTest
public class DeepSeekTest {
    @Resource
    private DsApiClient dsApiClient;
    @Resource
    private AiManager aiManager;
    @Test
    public void dstest(){
        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setUserContent("请你实现快速排序的代码");
        chatRequest.setSysTemContent("假如你是一位Java大神");
        chatRequest.setModel(CommonConstant.DS_CHAT_MODEL);
        try {
            DeepseekChatCompletionResponse chatCompletion = dsApiClient.getChatCompletion(chatRequest);
            System.out.println(chatCompletion.getChoices().get(0).getMessage().getContent());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    @Test
    public void yutets(){
        String s = aiManager.doChat("假如你是一位看了很多日本动画的肥宅,推荐10部日本动画");
        System.out.println(s);
    }
    @Test
    public void ssjjtest(){
        String code="{\n" +
                "  \"title\": {\n" +
                "    \"text\": \"网站用户增长情况\",\n" +
                "    \"subtext\": \"\"\n" +
                "  },\n" +
                "  \"tooltip\": {\n" +
                "    \"trigger\": \"axis\",\n" +
                "    \"axisPointer\": {\n" +
                "      \"type\": \"shadow\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"legend\": {\n" +
                "    \"data\": [\"用户数\"]\n" +
                "  },\n" +
                "  \"xAxis\": {\n" +
                "    \"data\": [\"1号\", \"2号\", \"3号\"]\n" +
                "  },\n" +
                "  \"yAxis\": {\n" +
                "    \"type\": \"value\",\n" +
                "    \"axisLabel\": {\n" +
                "      \"formatter\": \"{value} 人\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"series\": [\n" +
                "    {\n" +
                "      \"name\": \"用户数\",\n" +
                "      \"type\": \"line\",\n" +
                "      \"data\": [10, 20, 30]\n" +
                "    }\n" +
                "  ]\n" +
                "}\n";
        boolean b = InvalidEchartsUtil.checkEchartsTest(code);
        if (b){
            System.out.println("检验成功");
        }else {
            System.out.println("检验失败");
        }
    }
    @Test
    public void xinhuoTest(){
        String s = aiManager.sendMesToAIUseXingHuo("假如你是一位看了很多日本动画的肥宅,推荐10部日本动画");
        System.out.println(s);
    }
    @Test
    public void biXinhuoTest(){
        ChartGenResult genResult = ChartDataUtil.getGenResult(aiManager, "分析网站的用户增长速度", "日期,用户数\n" +
                "1号,10\n" +
                "2号,20\n" +
                "3号,30", "折线图");
        System.out.println(genResult.getGenChart());
        System.out.println(genResult.getGenResult());
        if (InvalidEchartsUtil.checkEchartsTest(genResult.getGenChart())){
            System.out.println("检验成功");
        }else {
            System.out.println("检验失败");
        }
    }
}
