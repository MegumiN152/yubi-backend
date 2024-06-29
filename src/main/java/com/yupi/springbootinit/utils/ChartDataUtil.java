package com.yupi.springbootinit.utils;

import cn.hutool.core.util.ObjectUtil;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.constant.CommonConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.manager.AiManager;
import com.yupi.springbootinit.model.dto.chart.ChartGenResult;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.service.ChartService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ChartDataUtil {
    @Resource
    private ChartService chartService;

    public static String changeDataToCSV(List<Map<String, Object>> chartOriginalData) {
        List<Set<String>> columnSets = chartOriginalData.stream()
                .map(Map::keySet)
                .collect(Collectors.toList());
        List<String> columnHeader = columnSets.stream()
                .map(column -> column.stream().filter(ObjectUtil::isNotNull).collect(Collectors.joining(",")))
                .collect(Collectors.toList());
        // 拿到对应的 value 拼接上
        List<String> columnDataList = chartOriginalData.stream().map(columnData -> {
            StringBuilder result = new StringBuilder();
            String[] headers = columnHeader.get(0).split(",");
            for (int i = 0; i < headers.length; i++) {
                String data = (String) columnData.get(headers[i]);
                result.append(data);
                if (i != headers.length - 1) {
                    result.append(",");
                }
            }
            result.append("\n");
            return result.toString();
        }).collect(Collectors.toList());
        // 将 columnDataList 中的数据添加到 stringJoiner
        StringJoiner stringJoiner = new StringJoiner("");
        stringJoiner.add(columnHeader.get(0)).add("\n");
        columnDataList.forEach(stringJoiner::add);
        return stringJoiner.toString();
    }
    public static ChartGenResult getGenResult(final AiManager aiManager, final  Chart chart) {
        String promote = buildUserInput(chart);
        String resultData = aiManager.doChat(promote, CommonConstant.BI_MODEL_ID);
        String[] splits = resultData.split("【【【【【");
        if (splits.length < 3) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 生成错误");
        }
        String genChart = splits[1].trim();
        String genResult = splits[2].trim();
        return new ChartGenResult(genChart, genResult);
    }
    public static String buildUserInput(Chart chart){
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
    /**
     * 获取 AI 生成结果
     * @param aiManager  AI 能力
     * @param goal
     * @param cvsData
     * @param chartType
     * @return
     */
    public static ChartGenResult getGenResult(final AiManager aiManager, final String goal, final String cvsData, final String chartType) {
        String promote = AiManager.PRECONDITION + "分析需求 " + goal + " \n原始数据如下: " + cvsData + "\n生成图标的类型是: " + chartType;
        String resultData = aiManager.sendMesToAIUseXingHuo(promote);
        log.info("AI 生成的信息: {}", resultData);
        ThrowUtils.throwIf(resultData.split("'【【【【【'").length < 3, ErrorCode.SYSTEM_ERROR);
        String genChart = resultData.split("'【【【【【'")[1].trim();
        String genResult = resultData.split("'【【【【【'")[2].trim();
        return new ChartGenResult(genChart, genResult);
    }

}
