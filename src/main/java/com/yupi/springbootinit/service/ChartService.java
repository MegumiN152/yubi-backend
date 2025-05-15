package com.yupi.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.model.dto.chart.ChartQueryRequest;
import com.yupi.springbootinit.model.dto.chart.ChartRegenRequest;
import com.yupi.springbootinit.model.dto.team.ChartAddToTeamRequest;
import com.yupi.springbootinit.model.entity.Chart;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.springbootinit.model.vo.BIResponse;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 *
 */
public interface ChartService extends IService<Chart> {
  public void createTable(Long chartId, MultipartFile multipartFile);

  public void saveChartData(MultipartFile file, String chartName) throws IOException;
  public void dropCollection(String collectionName);
  public void saveDataFromString(String data,String fileName);

    Page<Chart> pageTeamChart(ChartQueryRequest chartQueryRequest);

    BIResponse regenChartByAsyncMqFromTeam(ChartRegenRequest chartRegenRequest, HttpServletRequest request);

  QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest);

  boolean addChartToTeam(ChartAddToTeamRequest chartAddToTeamRequest, HttpServletRequest request);

  Page<Chart> searchMyCharts(ChartQueryRequest chartQueryRequest);
}
