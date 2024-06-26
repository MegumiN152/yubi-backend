package com.yupi.springbootinit.model.dto.chart;

import lombok.Data;

/**
 * @author 黄昊
 * @version 1.0
 **/
@Data
public class BiResponse {
    //生成的图表代码
    private String genChart;
    //生成的图表分析结果
    private String genResult;
    //图表id
    private Long chartId;
}
