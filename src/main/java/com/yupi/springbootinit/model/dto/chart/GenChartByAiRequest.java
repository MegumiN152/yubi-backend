package com.yupi.springbootinit.model.dto.chart;

import lombok.Data;

import java.io.Serializable;

/**
 * 文件上传请求
 *
 * @author <a href="https://github.com/MegumiN152">黄昊</a>
 * @from <a href="http://www.huanghao.icu/">GBC智能BI</a>
 */
@Data
public class GenChartByAiRequest implements Serializable {


    private static final long serialVersionUID = 1L;
    /**
     * 图表名称
     */
    private String name;
    /**
     * 分析目标
     */
    private String goal;
    /**
     * 图表类型
     */
    private String chartType;
}