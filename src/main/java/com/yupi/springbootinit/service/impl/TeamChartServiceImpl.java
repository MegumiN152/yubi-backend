package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.mapper.TeamChartMapper;
import com.yupi.springbootinit.model.entity.TeamChart;
import com.yupi.springbootinit.service.TeamChartService;
import org.springframework.stereotype.Service;

/**
 * @author 黄昊
 * @version 1.0
 **/
@Service
public class TeamChartServiceImpl extends ServiceImpl<TeamChartMapper, TeamChart> implements TeamChartService {
}
