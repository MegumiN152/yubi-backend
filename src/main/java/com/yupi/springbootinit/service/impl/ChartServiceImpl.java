package com.yupi.springbootinit.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.model.dto.chart.BiResponse;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.service.ChartService;
import com.yupi.springbootinit.mapper.ChartMapper;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService {
    @Resource
    private ChartMapper chartMapper;

    public void createTable(Long chartId, MultipartFile multipartFile) {
        String table_name = "chart_" + chartId;
        StringBuilder createTableSql = new StringBuilder("CREATE TABLE IF NOT EXISTS " + table_name + " (");
        List<Map<Integer, String>> list = null;
        try {
            list = EasyExcel.read(multipartFile.getInputStream())
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet()
                    .headRowNumber(0)
                    .doReadSync();
        } catch (IOException e) {
            log.error("表格处理错误");
            throw new RuntimeException(e);
        }
        //读取表头
        LinkedHashMap<Integer, String> headerMap = (LinkedHashMap) list.get(0);
        List<String> headerList = headerMap.values().stream().filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());
        for (String header : headerList) {
            createTableSql.append(header).append(" VARCHAR(255), ");
        }
        createTableSql.setLength(createTableSql.length() - 2); // 移除最后一个逗号
        createTableSql.append(");");
        chartMapper.executeSQL(createTableSql.toString());
        // 生成插入数据的SQL
        for (int i = 1; i < list.size(); i++) {
            StringBuilder insertSql = new StringBuilder("INSERT INTO " + table_name + " VALUES (");
            LinkedHashMap<Integer, String> dataMap = (LinkedHashMap) list.get(i);
            List<String> dataList = dataMap.values().stream().filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());
            for (String cell : dataList) {
                insertSql.append("'").append(cell).append("', ");
            }
            insertSql.setLength(insertSql.length() - 2); // 移除最后一个逗号
            insertSql.append(");");

            // 执行插入数据的SQL
            chartMapper.executeSQL(insertSql.toString());
        }
    }


}




