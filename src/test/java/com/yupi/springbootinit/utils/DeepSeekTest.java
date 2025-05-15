package com.yupi.springbootinit.utils;

import com.hh.deepseeksdk.client.DsApiClient;
import com.hh.deepseeksdk.model.ChatRequest;
import com.hh.deepseeksdk.model.DeepseekChatCompletionResponse;
import com.hh.deepseeksdk.model.SendMessage;
import com.yupi.springbootinit.constant.CommonConstant;

import com.yupi.springbootinit.manager.AiManager;
import com.yupi.springbootinit.model.dto.chart.ChartGenResult;
import com.yupi.springbootinit.model.dto.translate.TranslateRequest;
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
        //默认模型为deepseek-chat
        ChatRequest chatRequest = new ChatRequest();
        SendMessage sendMessage=new SendMessage();
        //设置具体内容
        sendMessage.setUserMessage(  "分析需求 网站用户增长速度分析\n" +
                "原始数据如下：num,day\n" +
                "3,12月12日\n" +
                "8,12月13日\n" +
                "5,12月14日\n" +
                "9,9月14日\n" +
                "生成的图表类型是：雷达图");
        //指定身份
        sendMessage.setSystemMessage("你是一个数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容：\n" +
                "分析需求：\n" +
                "{数据分析的需求或者目标}\n" +
                "原始数据：\n" +
                "{csv格式的原始数据，用,作为分隔符}\n" +
                "请根据这两部分内容，按照以下指定格式生成内容（此外不要输出任何多余的开头、结尾、注释）\n" +
                "【【【【【\n" +
                "{前端 Echarts V5 的 option 配置对象的json代码，合理地将数据进行可视化，不要生成任何多余的内容，比如注释}\n" +
                "【【【【【\n" +
                "{明确的数据分析结论、越详细越好，不要生成多余的注释}");
        try {
            DeepseekChatCompletionResponse chatCompletion = dsApiClient.getChatCompletion(chatRequest,sendMessage);
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
                "  \"radar\": {\n" +
                "    \"indicator\": [\n" +
                "      {\"name\": \"12月12日\", \"max\": 9},\n" +
                "      {\"name\": \"12月13日\", \"max\": 9},\n" +
                "      {\"name\": \"12月14日\", \"max\": 9},\n" +
                "      {\"name\": \"9月14日\", \"max\": 9}\n" +
                "    ]\n" +
                "  },\n" +
                "  \"series\": [\n" +
                "    {\n" +
                "      \"type\": 'radar',\n" +
                "      \"data\": [\n" +
                "        {\n" +
                "          \"value\": [3, 8, 5, 9],\n" +
                "          \"name\": '发射点'\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        boolean b = InvalidEchartsUtil.checkEchartsTest(code)&&ChartDataUtil.isJsonValid(code);
        System.out.println(ChartDataUtil.replaceJson(code));
        System.out.println("----");
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
        ChartGenResult genResult = ChartDataUtil.getGenResult(aiManager, "网站用户增长速度分析",  "num,day\n" +
                "3,12月12日\n" +
                "8,12月13日\n" +
                "5,12月14日\n" +
                "9,9月14日\n", "雷达图");
        System.out.println(genResult.getGenChart());
        System.out.println(genResult.getGenResult());
        if (InvalidEchartsUtil.checkEchartsTest(genResult.getGenChart())){
            System.out.println("检验成功");
        }else {
            System.out.println("检验失败");
        }

    }
//    @Test
//    public void TranslateTest(){
//        TranslateRequest translateRequest = new TranslateRequest();
//        translateRequest.setSourceLang("中文");
//        translateRequest.setTargetLang("日语");
//        translateRequest.setText("你好,我是来自中国的二次元肥宅,喜欢看动画,最喜欢的角色是来自《命运石之门》的牧濑红莉栖");
//        String translateResult = ChartDataUtil.getTranslateResult(aiManager, translateRequest);
//        System.out.println(translateResult);
//    }
}
