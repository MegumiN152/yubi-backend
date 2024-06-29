package com.yupi.springbootinit.bizmq;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rabbitmq.client.Channel;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.constant.CommonConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.manager.AiManager;
import com.yupi.springbootinit.model.dto.chart.ChartGenResult;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.model.enums.ResultEnum;
import com.yupi.springbootinit.service.ChartService;
import com.yupi.springbootinit.utils.ChartDataUtil;
import com.yupi.springbootinit.utils.ExcelUtils;
import com.yupi.springbootinit.utils.InvalidEchartsUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

import static com.yupi.springbootinit.utils.ChartDataUtil.buildUserInput;

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
            throwExceptionAndNackMessage(channel,deliveryTag);
        }
        long chartId = Long.parseLong(message);
        Chart chart = chartService.getById(chartId);
        if(chart==null){
            throwExceptionAndNackMessage(channel,deliveryTag);
        }
        Long userId = chart.getUserId();
        //检查用户任务计数器
        int userTaskCount=(int) getRunningTaskCount(userId);
        try {
            //检验当前用户执行任务数是否超过最大值
            if (userTaskCount<=BiMqConstant.MAX_CONCURRENT_CHARTS) {
                Chart updateChart = new Chart();
                updateChart.setId(chart.getId());
                updateChart.setStatus(ResultEnum.RUNNING.getDes());
                boolean b = chartService.updateById(updateChart);
                if (!b) {
                    handleChartUpdateError(chart.getId(), "图表执行中状态更新失败");
                    return;
                }
                //调用ai
                ChartGenResult result = ChartDataUtil.getGenResult(aiManager, chart.getGoal(), chart.getChartData(), chart.getChartType());
                String genchart=ChartDataUtil.replaceJson(result.getGenChart());
                if (!InvalidEchartsUtil.checkEchartsTest(genchart)){
                    handleChartUpdateError(chart.getId(), "ai生成的代码出错了");
                }
                Chart updateChartResult = new Chart();
                updateChartResult.setId(chart.getId());
                updateChartResult.setGenResult(result.getGenResult());
                updateChartResult.setGenChart(genchart);
                updateChartResult.setStatus(ResultEnum.SUCCEED.getDes());
                boolean b1 = chartService.updateById(updateChartResult);
                if (!b1) {
                    throwExceptionAndNackMessage(channel, deliveryTag);
                    return;
                }
                log.info("接收到的消息={}",message);
                channel.basicAck(deliveryTag,false);
                return;
            }
            channel.basicNack(deliveryTag,false,true);
        } catch (Exception e) {
            log.error(e.getMessage());
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

    }

    @RabbitListener(queues = {BiMqConstant.BI_DLX_QUEUE_NAME},ackMode = "MANUAL")
    public void receiveErrorMessage(String message,Channel channel,@Header(AmqpHeaders.DELIVERY_TAG) long deliverTag){
        if (StringUtils.isBlank(message)){
            throwExceptionAndNackMessage(channel,deliverTag);
        }
        log.info("receiveErrorMessage message = {}", message);
        Chart chart = chartService.getById(message);
        if (chart==null){
            throwExceptionAndNackMessage(channel,deliverTag);
        }
        Chart updateChart = new Chart();
        updateChart.setId(Long.parseLong(message));
        updateChart.setStatus(ResultEnum.FAILED.getDes());
        chartService.updateById(updateChart);
        try {
            channel.basicAck(deliverTag, false);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    private void throwExceptionAndNackMessage(Channel channel, long deliveryTag) {
        try {
            channel.basicNack(deliveryTag, false, false);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        throw new BusinessException(ErrorCode.SYSTEM_ERROR);
    }

    private void handleChartUpdateError(long chartId,String execMessage){
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chartId);
        updateChartResult.setStatus(ResultEnum.FAILED.getDes());
        updateChartResult.setExecMessage(execMessage);
        boolean b = chartService.updateById(updateChartResult);
        if (!b){
            log.error("更新图表状态失败"+chartId+" , "+execMessage);
        }
    }
    /**
     * 获取当前用户正在运行的任务数量，就算服务器出现问题，数据已经持久化到硬盘之中
     *
     * @param userId
     * @return
     */
    private long getRunningTaskCount(Long userId) {
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId).eq("status", ResultEnum.RUNNING.getDes());
        return chartService.count(queryWrapper);
    }

}
