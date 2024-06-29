package com.yupi.springbootinit.job.cycle;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yupi.springbootinit.manager.AiManager;
import com.yupi.springbootinit.mapper.ChartMapper;
import com.yupi.springbootinit.model.dto.chart.ChartGenResult;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.model.enums.ResultEnum;
import com.yupi.springbootinit.service.ChartService;
import com.yupi.springbootinit.utils.ChartDataUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author 黄昊
 * @version 1.0
 **/
@Component
@Slf4j
public class ReGenChartData{
    @Resource
    private ChartMapper chartMapper;
    @Resource
    private AiManager aiManager;
    @Resource
    private ChartService chartService;
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Scheduled(cron = "0 0/5 * * * ?")
    public void doUpdateFailedChart(){
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", ResultEnum.FAILED.getDes());
        List<Chart> failCharts = chartMapper.selectList(queryWrapper);
        failCharts.forEach(this::updateFailedChartAsync);
    }
    /**
     * 同步更新失败的图表
     */
    private void updateFailedChart(final Chart chart){
        Long chartId=chart.getId();
        String chartData = chart.getChartData();
        String goal = chart.getGoal();
        String chartType = chart.getChartType();
        ChartGenResult genResult = ChartDataUtil.getGenResult(aiManager, goal,chartData,chartType);
        Chart chart1 = new Chart();
        chart1.setGenChart(genResult.getGenChart());
        chart1.setId(chartId);
        chart1.setGenResult(genResult.getGenResult());
        chart1.setStatus(ResultEnum.SUCCEED.getDes());
        try {
            chartService.updateById(chart1);
        } catch (Exception e) {
            Chart chart2 = new Chart();
            chart2.setId(chartId);
            chart2.setStatus(ResultEnum.FAILED.getDes());
            chart2.setExecMessage(e.getMessage());
            chartService.updateById(chart2);
        }
    }
    /**
     * 异步更新失败的图表
     */
    private void updateFailedChartAsync(final Chart chart){
        CompletableFuture.runAsync(()->{
            updateFailedChart(chart);
        },threadPoolExecutor);
    }
}
