package com.yupi.springbootinit.config;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.read.listener.ReadListener;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapDataListener implements ReadListener<Map<Integer, String>> {
    private final MongoTemplate mongoTemplate;
    private final String chartName;
    private List<String> header;

    public MapDataListener(MongoTemplate mongoTemplate, String chartName) {
        this.mongoTemplate = mongoTemplate;
        this.chartName = chartName;
    }

    @Override
    public void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
        header = new ArrayList<>();
        for (Map.Entry<Integer, ReadCellData<?>> entry : headMap.entrySet()) {
            header.add(entry.getValue().getStringValue());
        }
    }

    @Override
    public void invoke(Map<Integer, String> data, AnalysisContext context) {
        Map<String, Object> dataMap = new HashMap<>();

        for (int i = 0; i < header.size(); i++) {
            String key = header.get(i);
            String value = data.get(i);

            // 只在 key 和 value 都不为 null 时才将其放入 dataMap 中
            if (key != null && value != null) {
                dataMap.put(key, value);
            }
        }

        mongoTemplate.save(dataMap, chartName);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // Do nothing after all data is analysed
    }
}
