package com.yupi.springbootinit.bizmq;

import com.rabbitmq.client.Channel;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.constant.CommonConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.manager.AiManager;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.service.ChartService;
import com.yupi.springbootinit.utils.ExcelUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author 黄昊
 * @version 1.0
 **/
@Component
@Slf4j
public class BiMessageConsumer {
    @Resource
    ChartService chartService;
    @Resource
    AiManager aiManager;

    @SneakyThrows
    @RabbitListener(queues = {BiMqConstant.BI_QUEUE_NAME},ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag){
        log.info("接受的消息为={}",message);
        if(StringUtils.isBlank(message)){
            channel.basicNack(deliveryTag,false,false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"消息为空");
        }
        long chartId = Long.parseLong(message);
        Chart chart = chartService.getById(chartId);
        if(chart==null){
            channel.basicNack(deliveryTag,false,false);
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"图表为空");
        }

        Chart updateChart = new Chart();
        updateChart.setId(chart.getId());
        updateChart.setStatus("running");
        boolean b = chartService.updateById(updateChart);
        if (!b){
            handleChartUpdateError(chart.getId(),"图表执行中状态更新失败");
            return;
        }
        //调用ai
        String result=aiManager.doChat(buildUserInput(chart), CommonConstant.BI_MODEL_ID);
        String[] split = result.split("【【【【【");
        if(split.length<3){
            handleChartUpdateError(chart.getId(),"AI生成错误");
            return;
        }
        String genChart=split[1].trim();
        String genResult=split[2].trim();
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chart.getId());
        updateChartResult.setGenResult(genResult);
        updateChartResult.setGenChart(genChart);
        updateChartResult.setStatus("succeed");
        boolean b1 = chartService.updateById(updateChartResult);
        if (!b1){
            handleChartUpdateError(chart.getId(),"更新图表成功状态失败");
            return;
        }
        log.info("接收到的消息={}",message);
        channel.basicAck(deliveryTag,false);
    }
    private void handleChartUpdateError(long chartId,String execMessage){
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chartId);
        updateChartResult.setStatus("failed");
        updateChartResult.setExecMessage(execMessage);
        boolean b = chartService.updateById(updateChartResult);
        if (!b){
            log.error("更新图表状态失败"+chartId+" , "+execMessage);
        }
    }
    private String buildUserInput(Chart chart){
        String goal=chart.getGoal();
        String chartType=chart.getChartType();
        String csvData=chart.getChartData();

        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求:").append("\n");
        String userGoal=goal;
        if (StringUtils.isNotBlank(chartType)){
            userGoal+=",请使用"+chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据:").append("\n");
        userInput.append(csvData).append("\n");
        return userInput.toString();
    }
}
