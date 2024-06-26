package com.yupi.springbootinit.service;

import com.yupi.springbootinit.model.entity.Chart;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 */
public interface ChartService extends IService<Chart> {
  public  void createTable(Long chartId, MultipartFile multipartFile);
}
