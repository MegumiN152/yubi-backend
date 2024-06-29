package com.yupi.springbootinit.manager;

/**
 * @author 黄昊
 * @version 1.0
 **/

import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import io.github.briqt.spark4j.SparkClient;
import io.github.briqt.spark4j.constant.SparkApiVersion;
import io.github.briqt.spark4j.model.SparkMessage;
import io.github.briqt.spark4j.model.SparkSyncChatResponse;
import io.github.briqt.spark4j.model.request.SparkRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 用于对接 AI 平台
 */
@Service
@Slf4j
public class AiManager {

    @Resource
    private SparkClient sparkClient;

    @Resource
    private YuCongMingClient yuCongMingClient;

    public static final String PRECONDITION = "你是一个数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容：\n" +
            "分析需求：\n" +
            "{数据分析的需求或者目标}\n" +
            "原始数据：\n" +
            "{csv格式的原始数据，用,作为分隔符}\n" +
            "请根据这两部分内容，严格按照以下指定格式生成内容（此外不要输出任何多余的开头、结尾、注释）同时不要使用这个符号 '】'\n" +
            "'【【【【【'\n" +
            "{前端 Echarts V5 的 option 配置对象 JSON 代码, 不要生成任何多余的内容，比如注释和代码块标记}\n" +
            "'【【【【【'\n" +
            "{明确的数据分析结论、越详细越好，不要生成多余的注释} \n"
            + "下面是一个具体的例子的模板："
            + "'【【【【【'\n"
            + "{\"xxx\": }"
            + "'【【【【【'\n" +
            "结论：";

    /**
     * AI 对话
     *
     * @param message
     * @return
     */
    public  String doChat(String message) {
        // 第三步，构造请求参数
        DevChatRequest devChatRequest = new DevChatRequest();
        // 模型id，尾后加L，转成long类型
        devChatRequest.setModelId(1654785040361893889L);
        devChatRequest.setMessage(message);
        // 第四步，获取响应结果
        BaseResponse<DevChatResponse> response = yuCongMingClient.doChat(devChatRequest);
        // 如果响应为null，就抛出系统异常，提示“AI 响应错误”
        if (response == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 响应错误");
        }
        return response.getData().getContent();
    }
    public  String doChat(String message,Long modelId) {
        // 第三步，构造请求参数
        DevChatRequest devChatRequest = new DevChatRequest();
        // 模型id，尾后加L，转成long类型
        devChatRequest.setModelId(modelId);
        devChatRequest.setMessage(message);
        // 第四步，获取响应结果
        BaseResponse<DevChatResponse> response = yuCongMingClient.doChat(devChatRequest);
        // 如果响应为null，就抛出系统异常，提示“AI 响应错误”
        if (response == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 响应错误");
        }
        return response.getData().getContent();
    }
    public String sendMesToAIUseXingHuo(final String content) {
        List<SparkMessage> messages = new ArrayList<>();
        messages.add(SparkMessage.userContent(content));
        // 构造请求
        SparkRequest sparkRequest = SparkRequest.builder()
                // 消息列表
                .messages(messages)
                // 模型回答的tokens的最大长度,非必传,取值为[1,4096],默认为2048
                .maxTokens(2048)
                // 核采样阈值。用于决定结果随机性,取值越高随机性越强即相同的问题得到的不同答案的可能性越高 非必传,取值为[0,1],默认为0.5
                .temperature(0.2)
                // 指定请求版本，默认使用最新2.0版本
                .apiVersion(SparkApiVersion.V3_5)
                .build();
        // 同步调用
        SparkSyncChatResponse chatResponse = sparkClient.chatSync(sparkRequest);
        String responseContent = chatResponse.getContent();
        log.info("星火 AI 返回的结果 {}", responseContent);
        return responseContent;
    }

}
