package com.yupi.springbootinit.model.dto.chart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChartRetryRequest implements Serializable {

    private static final long serialVersionUID = -4015423666971233788L;
    /**
     * 图标的 ID
     */
    private Long id;
    /**
     * 图标名称
     */
    private String name;

    /**
     * 分析目标
     */
    private String goal;

    /**
     * 图表数据
     */
    private String chartData;

    /**
     * 图表类型
     */
    private String chartType;
}