package com.yupi.springbootinit.model.dto.chart;

import lombok.Data;

@Data
public class ChartResult {
    private String genChart;
    private String genResult;
    private Long chartId;
}