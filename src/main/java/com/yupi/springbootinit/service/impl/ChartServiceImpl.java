package com.yupi.springbootinit.service.impl;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.bizmq.BiMessageProducer;
import com.yupi.springbootinit.bizmq.MQMessage;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.config.MapDataListener;
import com.yupi.springbootinit.constant.CommonConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.manager.RedisLimiterManager;
import com.yupi.springbootinit.model.dto.chart.ChartQueryRequest;
import com.yupi.springbootinit.model.dto.chart.ChartRegenRequest;
import com.yupi.springbootinit.model.dto.team.ChartAddToTeamRequest;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.model.entity.Team;
import com.yupi.springbootinit.model.entity.TeamChart;
import com.yupi.springbootinit.model.entity.User;
import com.yupi.springbootinit.model.vo.BIResponse;
import com.yupi.springbootinit.service.ChartService;
import com.yupi.springbootinit.mapper.ChartMapper;
import com.yupi.springbootinit.service.TeamChartService;
import com.yupi.springbootinit.service.TeamService;
import com.yupi.springbootinit.service.UserService;
import com.yupi.springbootinit.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.beans.BeanUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.io.InputStream;

/**
 *
 */
@Service
@Slf4j
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService {
    @Resource
    private ChartMapper chartMapper;

    @Resource
    private TeamChartService teamChartService;
    @Resource
    private UserService userService;

    @Resource
    private RedisLimiterManager redisLimiterManager;

    @Resource
    private BiMessageProducer biMessageProducer;

    @Resource
    private TeamService teamService;

    @Resource
    private MongoTemplate mongoTemplate;

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

    public void saveChartData(MultipartFile file, String chartName) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            EasyExcel.read(inputStream, new MapDataListener(mongoTemplate, chartName)).sheet().doRead();
        }
    }
    public void dropCollection(String collectionName) {
        mongoTemplate.getDb().getCollection(collectionName).drop();
    }
    public void saveDataFromString(String data,String fileName) {
        // Split the data by lines
        String[] lines = data.split("\n");

        // Extract headers from the first line
        List<String> headers = Arrays.asList(lines[0].split(","));

        // Iterate over the remaining lines to create Document objects
        for (int i = 1; i < lines.length; i++) {
            String[] values = lines[i].split(",");
            Document document = new Document();
            for (int j = 0; j < headers.size(); j++) {
                document.append(headers.get(j), values[j]);
            }
            // Save the Document to MongoDB
            mongoTemplate.save(document, fileName);
        }
    }
    @Override
    public Page<Chart> pageTeamChart(ChartQueryRequest chartQueryRequest) {
        Long teamId = chartQueryRequest.getTeamId();
        long current = chartQueryRequest.getCurrent();
        long pageSize = chartQueryRequest.getPageSize();
        String name = chartQueryRequest.getName();
        // 建立 SSE 连接
        Page<TeamChart> teamChartPage = teamChartService.page(new Page<>(current, pageSize),
                new QueryWrapper<TeamChart>().eq("teamId", teamId));
        if (CollectionUtils.isEmpty(teamChartPage.getRecords())) {
            return new Page<>();
        }
        List<Long> chartIds = teamChartPage.getRecords().stream()
                .map(TeamChart::getChartId).collect(Collectors.toList());
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        queryWrapper.in(CollectionUtils.isNotEmpty(chartIds), "id", chartIds);
        queryWrapper.like(StringUtils.isNotEmpty(name), "name", name);
        Page<Chart> chartPage = this.page(new Page<>(current, pageSize), queryWrapper);
        chartPage.setTotal(chartIds.size());
        return chartPage;
    }
    @Override
    public BIResponse regenChartByAsyncMqFromTeam(ChartRegenRequest chartRegenRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 参数校验
        Long chartId = chartRegenRequest.getId();
        String name = chartRegenRequest.getName();
        String goal = chartRegenRequest.getGoal();
        String chartData = chartRegenRequest.getChartData();
        String chartType = chartRegenRequest.getChartType();
        Long teamId = chartRegenRequest.getTeamId();
        ThrowUtils.throwIf(chartId == null || chartId <= 0, ErrorCode.PARAMS_ERROR, "图表不存在");
        ThrowUtils.throwIf(StringUtils.isBlank(name), ErrorCode.PARAMS_ERROR, "图表名称为空");
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "分析目标为空");
        ThrowUtils.throwIf(StringUtils.isBlank(chartData), ErrorCode.PARAMS_ERROR, "原始数据为空");
        ThrowUtils.throwIf(StringUtils.isBlank(chartType), ErrorCode.PARAMS_ERROR, "图表类型为空");
        ThrowUtils.throwIf(teamId == null, ErrorCode.PARAMS_ERROR, "队伍Id为空");
        // 查看重新生成的图标是否存在
        ChartQueryRequest chartQueryRequest = new ChartQueryRequest();
        chartQueryRequest.setId(chartId);
        Long chartCount = chartMapper.selectCount(this.getQueryWrapper(chartQueryRequest));
        if (chartCount <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图表不存在");
        }
        // 限流
        redisLimiterManager.doRateLimit(CommonConstant.REDIS_LIMITER_ID + userId);
        // 更改图表状态为wait
        Chart waitingChart = new Chart();
        BeanUtils.copyProperties(chartRegenRequest, waitingChart);
        waitingChart.setStatus("wait");
        boolean updateResult = this.updateById(waitingChart);
        // 将修改后的图表信息保存至数据库
        if (!updateResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "修改图表信息失败");
        }
        log.info("修改后的图表信息初次保存至数据库成功");
        // 初次保存成功，则向MQ投递消息
        trySendMessageByMq(chartId, teamId, userId);
        BIResponse biResponse = new BIResponse();
        biResponse.setChartId(chartId);
        return biResponse;
    }
    private void trySendMessageByMq(long chartId, long teamId) {
        MQMessage mqMessage = MQMessage.builder().chartId(chartId).teamId(teamId).build();
        String mqMessageJson = JSONUtil.toJsonStr(mqMessage);
        try {
            biMessageProducer.sendTeamMessage(mqMessageJson);
        } catch (Exception e) {
            log.error("图表成功保存至数据库，但是消息投递失败");
            Chart failedChart = new Chart();
            failedChart.setId(chartId);
            failedChart.setStatus("failed");
            boolean b = this.updateById(failedChart);
            if (!b) {
                throw new RuntimeException("修改图表状态信息为失败失败了");
            }
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "MQ 消息发送失败");
        }
    }
    private void trySendMessageByMq(long chartId, long teamId, long invokeUserId) {
        MQMessage mqMessage = MQMessage.builder().chartId(chartId).teamId(teamId).invokeUserId(invokeUserId).build();
        String mqMessageJson = JSONUtil.toJsonStr(mqMessage);
        try {
            biMessageProducer.sendTeamMessage(mqMessageJson);
        } catch (Exception e) {
            log.error("图表成功保存至数据库，但是消息投递失败");
            Chart failedChart = new Chart();
            failedChart.setId(chartId);
            failedChart.setStatus("failed");
            boolean b = this.updateById(failedChart);
            if (!b) {
                throw new RuntimeException("修改图表状态信息为失败失败了");
            }
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "MQ 消息发送失败");
        }
    }
    /**
     * 获取查询包装类
     *
     * @param chartQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        if (chartQueryRequest == null) {
            return queryWrapper;
        }

        Long id = chartQueryRequest.getId();
        String name = chartQueryRequest.getName();
        String goal = chartQueryRequest.getGoal();
        String chartType = chartQueryRequest.getChartType();
        Long userId = chartQueryRequest.getUserId();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();

        // 根据查询条件查询
        queryWrapper.eq(id != null && id > 0, "id", id);
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.eq(StringUtils.isNotBlank(goal), "goal", goal);
        queryWrapper.eq(StringUtils.isNotBlank(chartType), "chartType", chartType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }
    @Override
    public boolean addChartToTeam(ChartAddToTeamRequest chartAddToTeamRequest, HttpServletRequest request) {
        Long chartId = chartAddToTeamRequest.getChartId();
        Long teamId = chartAddToTeamRequest.getTeamId();
        Chart chart = this.getById(chartId);
        if (chart == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图表不存在");
        }
        Team team = teamService.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
        }
        TeamChart teamChart = TeamChart.builder().teamId(teamId).chartId(chartId).build();
        return teamChartService.save(teamChart);
    }
    @Override
    public Page<Chart> searchMyCharts(ChartQueryRequest chartQueryRequest) {
        String name = chartQueryRequest.getName();
        long current = chartQueryRequest.getCurrent();
        long pageSize = chartQueryRequest.getPageSize();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        queryWrapper.and(qw -> qw.like(StringUtils.isNotBlank(name), "name", name).or().like(StringUtils.isNotBlank(name), "chartType", name));
        Page<Chart> page = this.page(new Page<>(current, pageSize), queryWrapper);
        return page;
    }

}




